import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Initialize User Manager, Movie Manager, Showtime Manager, and Scanner
        UserManager userManager = new UserManager();
        MovieManager movieManager = new MovieManager();
        ShowtimeManager showtimeManager = new ShowtimeManager();
        Scanner scanner = new Scanner(System.in);
        User currentUser = null;

        // Add a default Admin account directly
        User defaultAdmin = new User("admin", "admin123", "Admin");
        userManager.addUserDirectly(defaultAdmin);

        while (true) {
            // Show the main menu
            System.out.println("\n--- Movie Ticket Booking System ---");
            System.out.println("1. Register as User");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    // Regular User Registration
                    System.out.print("Enter username: ");
                    String regUsername = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String regPassword = scanner.nextLine();

                    userManager.registerUser(regUsername, regPassword, "User");
                    break;

                case 2:
                    // Login
                    System.out.print("Enter username: ");
                    String loginUsername = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String loginPassword = scanner.nextLine();

                    currentUser = userManager.loginUser(loginUsername, loginPassword);

                    // Differentiate user experience
                    if (currentUser != null) {
                        if (currentUser.getRole().equalsIgnoreCase("Admin")) {
                            adminMenu(scanner, userManager, currentUser, movieManager, showtimeManager);
                        } else {
                            userMenu(scanner, currentUser, movieManager, showtimeManager);
                        }
                    }
                    break;

                case 3:
                    // Exit
                    System.out.println("Thank you for using the Movie Ticket Booking System. Goodbye!");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // User-specific menu for booking actions
    private static void userMenu(Scanner scanner, User user, MovieManager movieManager, ShowtimeManager showtimeManager) {
        while (true) {
            System.out.println("\n--- User Menu ---");
            System.out.println("1. View Booking History");
            System.out.println("2. Search for Movies");
            System.out.println("3. Make a Booking");
            System.out.println("4. Cancel a Booking");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");
            int userChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (userChoice) {
                case 1:
                    user.viewBookingHistory();
                    break;

                case 2:
                    // Search for movies
                    System.out.println("Search Movies By:");
                    System.out.println("1. Genre");
                    System.out.println("2. Rating");
                    System.out.println("3. Title Keyword");
                    System.out.print("Enter your choice: ");
                    int searchChoice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    switch (searchChoice) {
                        case 1:
                            System.out.print("Enter genre to search: ");
                            String genre = scanner.nextLine();
                            movieManager.searchMoviesByGenre(genre);
                            break;
                        case 2:
                            System.out.print("Enter minimum rating (0 to 10): ");
                            double rating = scanner.nextDouble();
                            scanner.nextLine();
                            movieManager.searchMoviesByRating(rating);
                            break;
                        case 3:
                            System.out.print("Enter title keyword to search: ");
                            String keyword = scanner.nextLine();
                            movieManager.searchMoviesByTitle(keyword);
                            break;
                        default:
                            System.out.println("Invalid choice.");
                    }
                    break;

                case 3:
                    // Display available movies
                    movieManager.listMovies();
                    System.out.print("Select the index of the movie you want to book: ");
                    int movieIndex = scanner.nextInt();
                    scanner.nextLine();
                    Movie selectedMovie = movieManager.getMovie(movieIndex);
                    if (selectedMovie != null) {
                        // Display showtimes for the selected movie
                        System.out.println("Available showtimes for " + selectedMovie.getTitle() + ":");
                        showtimeManager.listShowtimesForMovie(selectedMovie);
                        System.out.print("Select the index of the showtime you want to book: ");
                        int showtimeIndex = scanner.nextInt();
                        scanner.nextLine();
                        Showtime selectedShowtime = showtimeManager.getShowtime(showtimeIndex);
                        if (selectedShowtime != null) {
                            // Display available seats for the selected showtime
                            List<Seat> availableSeats = selectedShowtime.getAvailableSeats();
                            if (!availableSeats.isEmpty()) {
                                System.out.println("Available Seats:");
                                for (Seat seat : availableSeats) {
                                    System.out.println(seat); // Uses overridden toString method
                                }

                                // Ask user for seat selection
                                System.out.print("Enter the seat numbers you want to book (comma-separated, e.g., 1,2): ");
                                String seatInput = scanner.nextLine();
                                String[] seatNumbers = seatInput.split(",");

                                // Prepare the list of seats to book
                                List<Seat> seatsToBook = new ArrayList<>();
                                for (String seatNumber : seatNumbers) {
                                    try {
                                        int seatNum = Integer.parseInt(seatNumber.trim());
                                        Seat seat = availableSeats.stream()
                                                .filter(s -> s.getSeatNumber() == seatNum && !s.isBooked())
                                                .findFirst()
                                                .orElse(null);
                                        if (seat != null) {
                                            seatsToBook.add(seat);
                                        } else {
                                            System.out.println("Seat " + seatNum + " is not available. Skipping this seat.");
                                        }
                                    } catch (NumberFormatException e) {
                                        System.out.println("Invalid seat number: " + seatNumber + ". Skipping this seat.");
                                    }
                                }

                                if (!seatsToBook.isEmpty()) {
                                    Booking booking = new Booking(user, selectedShowtime, seatsToBook);
                                    Payment payment = selectPaymentMethod(scanner, user);

                                    if (payment != null) {
                                        booking.confirmBooking(payment);
                                    }
                                } else {
                                    System.out.println("No valid seats selected. Booking canceled.");
                                }
                            } else {
                                System.out.println("No available seats for the selected showtime.");
                            }
                        } else {
                            System.out.println("Invalid showtime selection.");
                        }
                    } else {
                        System.out.println("Invalid movie selection.");
                    }
                    break;

                case 4:
                    System.out.println("Canceling the latest booking...");
                    if (!user.getBookingHistory().isEmpty()) {
                        Booking lastBooking = user.getBookingHistory().get(user.getBookingHistory().size() - 1);
                        lastBooking.cancelBooking();
                    } else {
                        System.out.println("No bookings to cancel.");
                    }
                    break;

                case 5:
                    System.out.println("Logging out...");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Admin-specific menu
    private static void adminMenu(Scanner scanner, UserManager userManager, User admin, MovieManager movieManager, ShowtimeManager showtimeManager) {
        AdminManager adminManager = new AdminManager(movieManager, showtimeManager);

        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Register a New Admin");
            System.out.println("2. Manage Movies");
            System.out.println("3. Manage Showtimes");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            int adminChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (adminChoice) {
                case 1:
                    System.out.print("Enter new Admin username: ");
                    String newAdminUsername = scanner.nextLine();
                    System.out.print("Enter new Admin password: ");
                    String newAdminPassword = scanner.nextLine();
                    userManager.addAdmin(admin, newAdminUsername, newAdminPassword);
                    break;

                case 2:
                    adminManager.manageMovies(scanner);
                    break;

                case 3:
                    adminManager.manageShowtimes(scanner);
                    break;

                case 4:
                    System.out.println("Logging out...");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Modular method for selecting a payment method
    private static Payment selectPaymentMethod(Scanner scanner, User user) {
        System.out.println("\nChoose Payment Method:");
        System.out.println("1. Credit Card");
        System.out.println("2. PayPal");
        System.out.println("3. Wallet");
        System.out.print("Enter your choice: ");
        int paymentChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        Payment payment;
        switch (paymentChoice) {
            case 1:
                payment = new CreditCardPayment(20.00, "1234-5678-9012-3456", user.getUsername());
                break;
            case 2:
                System.out.print("Enter PayPal email: ");
                String email = scanner.nextLine();
                payment = new PayPalPayment(20.00, email);
                break;
            case 3:
                System.out.print("Enter Wallet ID: ");
                String walletID = scanner.nextLine();
                payment = new WalletPayment(20.00, walletID);
                break;
            default:
                System.out.println("Invalid choice, payment canceled.");
                payment = null;
        }

        return payment;
    }
}