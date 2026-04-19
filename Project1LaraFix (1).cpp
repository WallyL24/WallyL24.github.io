#include <iostream>
#include <string>
#include <limits>
#include <vector>

using namespace std;

// stores all client-related information
// in one organized record 
struct Client {
    string name;
    int serviceChoice;
};

// vector is used to store multiple client records.
// makes the program easier to expand and maintain.
vector<Client> clients = {
    {"Bob Jones", 1},
    {"Sara Davis", 2},
    {"Amy Friendly", 1},
    {"Johnny Smith", 1},
    {"Carol Spears", 2}
};

// stores the username for login
string username;

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

    // Loop through the vector and display each client record
    for (int i = 0; i < clients.size(); i++) {
        cout << i + 1 << ". " << clients[i].serviceChoice << endl;
    }
}

// Change function
void ChangeCustomerChoice() {
    int clientNumber;
    int newService;

    // VULNERABILITY #4: No input validation or cin fail-state handling.
    // Non-numeric input or values outside 1–5 are not handled safely.
    // FIX APPLIED: Validate client number input and handle cin fail state
    cout << "Enter the number of the client that you wish to change (1-5)" << endl;
    while (!(cin >> clientNumber) || clientNumber < 1 || clientNumber > clients.size()) {
        cout << "Invalid client number. Please enter a number between 1 and " << clients.size() << endl;
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

    // update the selected client's service choice
    clients[clientNumber - 1].serviceChoice = newService;
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