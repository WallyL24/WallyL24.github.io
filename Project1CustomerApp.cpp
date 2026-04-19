#include <iostream>
#include <string>
#include <limits>

using namespace std;

// VULNERABILITY #1: Unsafe fixed-size buffer for username.
// Using a char array with cin >> username allows buffer overflow
// if the user enters more than 63 characters.
// FIX APPLIED: Use string instead of fixed-size char array to prevent buffer overflow
string username;

int num1 = 1;
int num2 = 2;
int num3 = 1;
int num4 = 1;
int num5 = 2;

const char* name1 = "Bob Jones";
const char* name2 = "Sarah Davis";
const char* name3 = "Amy Friendly";
const char* name4 = "Johnny Smith";
const char* name5 = "Carol Spears";

// Login function (matches assembly behavior)
int CheckUserPermissionAccess() {
    string entered = "";

    cout << "Enter your username: " << endl;

    // VULNERABILITY #1 (continued): No length checking on input.
    // This can overflow the username buffer and overwrite memory.
    // FIX APPLIED: Use string instead of fixed-size char array to prevent buffer overflow
    cin >> username;

    cout << "Enter your password: " << endl;
    cin >> entered;

    // VULNERABILITY #2: Hardcoded and weak password.
    // The password "123" is stored in plain text and can be extracted
    // from the binary using tools like strings or a debugger.
    // It is also very easy to guess.
    // FIX NOT APPLITED: Still hardcoded password
    if (entered.compare("123") == 0) {
        return 1;
    } else {
        return 2;
    }
}

// Display function
void DisplayInfo() {
    cout << "  Client's Name    Service Selected (1 = Brokerage, 2 = Retirement)" << endl;

    cout << "1. " << name1 << " selected option " << num1 << endl;
    cout << "2. " << name2 << " selected option " << num2 << endl;
    cout << "3. " << name3 << " selected option " << num3 << endl;
    cout << "4. " << name4 << " selected option " << num4 << endl;
    cout << "5. " << name5 << " selected option " << num5 << endl;
}

// Change function
void ChangeCustomerChoice() {
    int clientNumber;
    int newService;

    // VULNERABILITY #4: No input validation or cin fail-state handling.
    // Non-numeric input or values outside 1–5 are not handled safely.
    // FIX APPLIED: Validate client number input and handle cin fail state
    cout << "Enter the number of the client that you wish to change (1-5)" << endl;
    while (!(cin >> clientNumber) || clientNumber < 1 || clientNumber > 5) {
        cout << "Invalid client number. Please enter a number between 1 and 5." << endl;
        cin.clear();
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
    }

    // VULNERABILITY #4 (continued): No validation for service choice.
    // Values other than 1 or 2 are accepted and stored, breaking data integrity.
    // FIX APPLIED: Validate service choice input and enforce valid range
    cout << "Please enter the client's new service choice (1 = Brokerage, 2 = Retirement)" << endl;
    while (!(cin >> newService) || (newService != 1 && newService != 2)) {
        cout << "Invalid service choice. Please enter 1 or 2." << endl;
        cin.clear();
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
    }

    if (clientNumber == 1) num1 = newService;
    else if (clientNumber == 2) num2 = newService;
    else if (clientNumber == 3) num3 = newService;
    else if (clientNumber == 4) num4 = newService;
    else if (clientNumber == 5) num5 = newService;
}

// Main function
int main() {
    cout << "Hello! Welcome to our Investment Company" << endl;

    int access = 0;
    int attempts = 0;    // Added attempts variable to fix the vulnerability of unlimited login attempts


    // VULNERABILITY #3: Unlimited login attempts.
    // The program allows infinite password guesses, enabling brute-force attacks.

    // VULNERABILITY #6: No input validation or cin fail-state handling for menu choice.
    // Non-numeric input or values outside 1–3 are not handled safely.

    // VULNERABILITY #7: No handling for invalid menu choices.
    // The program just loops without informing the user of bad input.

    // FIX APPLIED: Limit login attempts to prevent brute-force attacks
    while (attempts < 3) {
        access = CheckUserPermissionAccess();
        if (access == 1) {
            break;
        }
        cout << "Invalid Password. Please try again" << endl;
        attempts++;
    }

    // Progam exits after failed attempts
    if (access != 1) {
        cout << "Too many failed login attempts. Exiting program." << endl;
        return 0;
    }

    int choice = 0;

    while (choice != 3) {
        cout << "What would you like to do?" << endl;
        cout << "DISPLAY the client list (enter 1)" << endl;
        cout << "CHANGE a client's choice (enter 2)" << endl;
        cout << "Exit the program.. (enter 3)" << endl;

        // FIX APPLIED: Validate menu input and handle cin fail state
        while (!(cin >> choice) || choice < 1 || choice > 3) {
            cout << "Invalid menu choice. Please enter 1, 2, or 3." << endl;
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
        }

        if (choice == 1) {
            DisplayInfo();
        }
        else if (choice == 2) {
            ChangeCustomerChoice();
        }
        // choice == 3 exits loop
    }

    return 0;
}