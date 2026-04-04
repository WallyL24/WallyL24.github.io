# Enhanced Thermostat Code (CS 499 Milestone Two)
# This version improves the original design by separating the program
# into smaller modules for sensor input, display output, LED control,
# serial communication, and thermostat logic.

from time import sleep
from datetime import datetime
from threading import Thread
from math import floor

from statemachine import StateMachine, State

import board
import adafruit_ahtx0
import digitalio
import adafruit_character_lcd.character_lcd as characterlcd
import serial

from gpiozero import Button, PWMLED

DEBUG = True

# -----------------------------
# Constants
# -----------------------------
STATE_OFF = "off"
STATE_HEAT = "heat"
STATE_COOL = "cool"

DEFAULT_SETPOINT = 72

SERIAL_PORT = "/dev/ttyS0"
SERIAL_BAUDRATE = 115200

RED_LED_PIN = 18
BLUE_LED_PIN = 23

STATE_BUTTON_PIN = 24
INCREASE_BUTTON_PIN = 25
DECREASE_BUTTON_PIN = 12


# -----------------------------
# Sensor Module
# -----------------------------
class TemperatureSensor:
    """
    Handles temperature sensor setup and temperature conversion.
    This keeps sensor-specific work separate from thermostat logic.
    """

    def __init__(self):
        # Initialize I2C and sensor hardware
        self.i2c = board.I2C()
        self.sensor = adafruit_ahtx0.AHTx0(self.i2c)

    def get_fahrenheit(self):
        """
        Read the current temperature from the sensor and convert it
        from Celsius to Fahrenheit.
        Returns None if the sensor read fails.
        """
        try:
            temp_c = self.sensor.temperature
            return ((9 / 5) * temp_c) + 32
        except Exception as e:
            if DEBUG:
                print("Sensor error:", e)
            return None


# -----------------------------
# Display Module
# -----------------------------
class ManagedDisplay:
    """
    Handles the LCD display setup and screen updates.
    This keeps all display-related behavior in one place.
    """

    def __init__(self):
        # Configure LCD pins
        self.lcd_rs = digitalio.DigitalInOut(board.D17)
        self.lcd_en = digitalio.DigitalInOut(board.D27)
        self.lcd_d4 = digitalio.DigitalInOut(board.D5)
        self.lcd_d5 = digitalio.DigitalInOut(board.D6)
        self.lcd_d6 = digitalio.DigitalInOut(board.D13)
        self.lcd_d7 = digitalio.DigitalInOut(board.D26)

        # Create a 16x2 LCD display object
        self.lcd = characterlcd.Character_LCD_Mono(
            self.lcd_rs,
            self.lcd_en,
            self.lcd_d4,
            self.lcd_d5,
            self.lcd_d6,
            self.lcd_d7,
            16,
            2
        )

        # Clear the LCD when the program starts
        self.lcd.clear()

    def update(self, message):
        """Clear the LCD and display new text."""
        self.lcd.clear()
        self.lcd.message = message

    def cleanup(self):
        """Clear the display before program exit."""
        self.lcd.clear()


# -----------------------------
# LED Module
# -----------------------------
class LEDController:
    """
    Handles the thermostat indicator LEDs.
    This keeps hardware output separate from decision-making logic.
    """

    def __init__(self):
        self.red = PWMLED(RED_LED_PIN)
        self.blue = PWMLED(BLUE_LED_PIN)

    def off(self):
        """Turn both LEDs off."""
        self.red.off()
        self.blue.off()

    def heat(self, active):
        """
        Show heat status.
        Pulse red LED if heating is active, otherwise keep it solid.
        """
        self.blue.off()
        if active:
            self.red.pulse()
        else:
            self.red.on()

    def cool(self, active):
        """
        Show cool status.
        Pulse blue LED if cooling is active, otherwise keep it solid.
        """
        self.red.off()
        if active:
            self.blue.pulse()
        else:
            self.blue.on()


# -----------------------------
# Serial Module
# -----------------------------
class SerialReporter:
    """
    Handles serial communication to report thermostat status.
    """

    def __init__(self):
        try:
            # Open the serial port for status reporting
            self.ser = serial.Serial(SERIAL_PORT, SERIAL_BAUDRATE, timeout=1)
        except Exception as e:
            self.ser = None
            if DEBUG:
                print("Serial initialization error:", e)

    def send(self, state, temp, setpoint):
        """
        Send the thermostat state, temperature, and setpoint
        over the serial port if available.
        """
        if self.ser:
            try:
                msg = f"{state},{temp},{setpoint}\n"
                self.ser.write(msg.encode())
            except Exception as e:
                if DEBUG:
                    print("Serial send error:", e)


# -----------------------------
# Thermostat Logic
# -----------------------------
class Thermostat(StateMachine):
    """
    Main thermostat controller.
    This class manages thermostat states and coordinates the
    sensor, display, LEDs, and serial output.
    """

    # Define thermostat states
    off = State(initial=True)
    heat = State()
    cool = State()

    # Define how the state button cycles through states
    cycle = off.to(heat) | heat.to(cool) | cool.to(off)

    def __init__(self, sensor, display, leds, serial_reporter):
        super().__init__()
        self.sensor = sensor
        self.display = display
        self.leds = leds
        self.serial = serial_reporter
        self.setpoint = DEFAULT_SETPOINT
        self.running = True

    def get_temp(self):
        """
        Get the current temperature as an integer Fahrenheit value.
        Returns None if the sensor read fails.
        """
        temp = self.sensor.get_fahrenheit()
        return floor(temp) if temp is not None else None

    def update_leds(self):
        """
        Update LED behavior based on current thermostat state
        and the relationship between temperature and setpoint.
        """
        temp = self.get_temp()

        # If the sensor failed, turn LEDs off to avoid showing incorrect state
        if temp is None:
            self.leds.off()
            return

        # Heat mode: pulse if below setpoint, solid if at/above setpoint
        if self.current_state.id == STATE_HEAT:
            self.leds.heat(temp < self.setpoint)

        # Cool mode: pulse if above setpoint, solid if at/below setpoint
        elif self.current_state.id == STATE_COOL:
            self.leds.cool(temp > self.setpoint)

        # Off mode: no lights
        else:
            self.leds.off()

    def display_loop(self):
        """
        Background thread that updates the LCD display and sends
        periodic serial status updates.
        """
        counter = 0

        while self.running:
            # Get current date/time for top LCD line
            now = datetime.now().strftime("%m/%d %H:%M")
            temp = self.get_temp()

            # Display an error if the sensor cannot be read
            if temp is None:
                line2 = "Sensor Error"
            else:
                # Alternate second line between temperature and system state
                if counter % 10 < 5:
                    line2 = f"Temp: {temp}F"
                else:
                    line2 = f"{self.current_state.id} {self.setpoint}F"

            # Update LCD display
            self.display.update(now + "\n" + line2)

            # Send status update every 30 cycles
            if counter % 30 == 0 and temp is not None:
                self.serial.send(self.current_state.id, temp, self.setpoint)

            counter += 1
            sleep(1)

    def start(self):
        """Start the background display thread."""
        Thread(target=self.display_loop, daemon=True).start()


# -----------------------------
# Setup
# -----------------------------
# Create system components
sensor = TemperatureSensor()
display = ManagedDisplay()
leds = LEDController()
serial_reporter = SerialReporter()

# Create and start thermostat controller
thermo = Thermostat(sensor, display, leds, serial_reporter)
thermo.start()

# -----------------------------
# Button Configuration
# -----------------------------
# State button cycles through off -> heat -> cool
Button(STATE_BUTTON_PIN).when_pressed = thermo.cycle

# Increase button raises setpoint by 1 degree
Button(INCREASE_BUTTON_PIN).when_pressed = lambda: setattr(
    thermo, "setpoint", thermo.setpoint + 1
)

# Decrease button lowers setpoint by 1 degree
Button(DECREASE_BUTTON_PIN).when_pressed = lambda: setattr(
    thermo, "setpoint", thermo.setpoint - 1
)

# -----------------------------
# Main Loop
# -----------------------------
try:
    while True:
        # Keep LED state current while program runs
        thermo.update_leds()
        sleep(2)

except KeyboardInterrupt:
    # Stop display thread and clean up on exit
    print("Exiting...")
    thermo.running = False
    display.cleanup()