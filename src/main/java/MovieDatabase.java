import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
/*
umožňuje uživatelům se přihlásit a zobrazit seznam filmů. Poskytuje přihlašovací formulář, který přijímá uživatelské jméno a heslo a pokouší
se je porovnat s položkou v seznamu uživatelů. Pokud je přihlášení úspěšné, je aktuální uživatel uložen a uživateli je zobrazena databáze filmů.
Pokud přihlášení není úspěšné, je zobrazena chybová zpráva. Databáze filmů je tabulka, která zobrazuje název, rok,
režiséra. Uživatel také může vyhledávat filmy podle názvu a filtrovat výsledky podle režiséra nebo roku.
 */
public class MovieDatabase extends Application {
    // Soubor, ve kterém budou filmy a uživatelé uloženy
    private static final File MOVIES_FILE = new File("src/main/resources/movies.xml");
    private static final File USERS_FILE = new File("src/main/resources/users.xml");

    // list filmů a uživatelů
    private List<Movie> movies = new ArrayList<>();

    private List<User> users = new ArrayList<>();

    // idea ukazuje ze currentUser neni pouzity ale NEMAZAT
    private User currentUser;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // načte filmy a uživatele z XML souborů
        loadMovies();
        loadUsers();


// Vytvoření login fieldu
        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Username");

        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Password");


        /*
        Metoda setOnAction tlačítka loginButton slouží k určení event handler, která se zavolá po kliknutí na tlačítko.
        Event handler zkontroluje, zda jsou zadané uživatelské jméno a heslo správné, a to tak, že filtruje seznam uživatelů a
        vyhledá uživatele se zadaným uživatelským jménem a heslem. Pokud je uživatel nalezen, je pole currentUser nastaveno na
        tohoto uživatele a zobrazí se databáze filmů. Pokud není nalezen žádný uživatel, zobrazí se chybová zpráva.
        Uživatel se tak může přihlásit do aplikace a získat přístup k databázi filmů.
         */
        Button loginButton = new Button("Login");
        loginButton.setOnAction(event -> {
            // Kontrola zdali je username a password spravne
            User user = users.stream()
                    .filter(u -> u.getUsername().equals(usernameInput.getText()) &&
                            u.getPassword().equals(passwordInput.getText()))
                    .findFirst()
                    .orElse(null);

            if (user != null) {
                // pokud je login uspesny, ulozi to aktualniho usera a ukaze filmovou databazi
                currentUser = user;
                showMovieDatabase(stage);
            } else {
                // error pokud spatnej login
                Alert alert = new Alert(Alert.AlertType.ERROR, "Neplatné uživatelské jméno nebo heslo");
                alert.show();
            }
        });

        Button registerButton = new Button("Registrace");
        registerButton.setOnAction(event -> showRegistrationForm(stage));
        VBox loginForm = new VBox(usernameInput, passwordInput, loginButton, registerButton);
        loginForm.setSpacing(10);
        loginForm.setPadding(new Insets(10));

        // Nastavení scény a zobrazení přihlašovacího formuláře
        stage.setScene(new Scene(loginForm));
        stage.show();

    }

    private void showMovieDatabase(Stage stage) {
        // Vytvoření tabulky filmů
        TableView<Movie> movieTable = new TableView<>();

        TableColumn<Movie, String> titleColumn = new TableColumn<>("Název");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Movie, Integer> yearColumn = new TableColumn<>("Rok");
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<Movie, String> directorColumn = new TableColumn<>("Režisér");
        directorColumn.setCellValueFactory(new PropertyValueFactory<>("director"));

        TableColumn<Movie, Integer> ratingColumn = new TableColumn<>("Hodnocení");
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("averageRating"));

        movieTable.getColumns().addAll(titleColumn, yearColumn, directorColumn, ratingColumn);

        // Nastavení položek v tabulce na seznam filmů
        movieTable.setItems(FXCollections.observableArrayList(movies));

        // Vytvoření vyhledávacího panelu
        TextField searchBar = new TextField();
        searchBar.setPromptText("Hledej");
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filtrování filmů na základě vyhledávacího dotazu
            List<Movie> filteredMovies = movies.stream()
                    .filter(movie -> movie.getTitle().contains(newValue) ||
                            Integer.toString(movie.getYear()).contains(newValue) ||
                            movie.getDirector().contains(newValue))
                    .collect(Collectors.toList());

            // Nastavení položek v tabulce na filtrovaný seznam filmů
            movieTable.setItems(FXCollections.observableArrayList(filteredMovies));
        });




        // Vytvoření formuláře pro přidání filmu
        TextField titleInput = new TextField();
        titleInput.setPromptText("Název");

        TextField yearInput = new TextField();
        yearInput.setPromptText("Rok");

        TextField directorInput = new TextField();
        directorInput.setPromptText("Režisér");

        Button addButton = new Button("Přidat");
        addButton.setOnAction(event -> {
            // Vytvoření nového filmu se vstupními hodnotami
            Movie movie = new Movie(titleInput.getText(), Integer.parseInt(yearInput.getText()), directorInput.getText());

            // Přidat film do seznamu
            movies.add(movie);

            // Vymazání vstupních polí
            titleInput.clear();
            yearInput.clear();
            directorInput.clear();

            // Aktualizujte tabulku tak, aby zobrazovala nově přidaný film
            movieTable.setItems(FXCollections.observableArrayList(movies));
        });

        HBox addMovieForm = new HBox(titleInput, yearInput, directorInput, addButton);
        addMovieForm.setSpacing(10);

        // Vytvoření tlačítka pro odhlášení
        Button logoutButton = new Button("Odhlášení");
        logoutButton.setOnAction(event -> {
            // Vymazání aktuálního uživatele a zobrazení přihlašovacího formuláře
            currentUser = null;
            stop();
            start(stage);
        });

        VBox container = new VBox(movieTable, searchBar, addMovieForm, logoutButton);
        container.setSpacing(10);
        container.setPadding(new Insets(10));

        // Nastavení scény a zobrazení filmové databáze
        stage.setScene(new Scene(container));
        stage.show();
    }
/*
Metody loadMovies a loadUsers slouží k načtení seznamu filmů a uživatelů ze souborů XML, ve kterých jsou uloženy.
Třída XmlMapper se používá k analýze dat XML a vytvoření objektů Seznam filmů nebo Uživatelé. Metoda constructCollectionType
se používá k určení typu Seznamu, který bude vytvořen. To umožňuje mapovači XML správně "deserializovat"
data XML na seznam objektů. Pole Filmy a Uživatelé jsou pak nastavena na seznam objektů, který byl načten ze souborů XML.
 */

    private void loadMovies() {
        XmlMapper xmlMapper = new XmlMapper();
        try {
            movies = xmlMapper.readValue(MOVIES_FILE, xmlMapper.getTypeFactory().constructCollectionType(List.class, Movie.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadUsers() {
        XmlMapper xmlMapper = new XmlMapper();
        try {
            users = xmlMapper.readValue(USERS_FILE, xmlMapper.getTypeFactory().constructCollectionType(List.class, User.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        // Uloží filmy do XML souboru
        saveMovies();
    }


    private void saveMovies() {
        XmlMapper xmlMapper = new XmlMapper();
        try {
            xmlMapper.writeValue(MOVIES_FILE, movies);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void saveUsers() {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.writeValue(USERS_FILE, users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showRegistrationForm(Stage stage) {
        // Vytvoření registračního formuláře
        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Username");

        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Heslo");


        Button registerButton = new Button("Registrace");
        registerButton.setOnAction(event -> {
            //  Zkontrolujte, zda je zadané uživatelské jméno již obsazeno.
            boolean usernameTaken = users.stream()
                    .anyMatch(u -> u.getUsername().equals(usernameInput.getText()));

            if (usernameTaken) {
                // Pokud je uživatelské jméno obsazeno, zobrazí se chybová zpráva.
                Alert alert = new Alert(Alert.AlertType.ERROR, "Uživatelské jméno je již obsazeno");
                alert.show();
            } else {
                // Pokud uživatelské jméno není obsazeno, přidejte nového uživatele do seznamu uživatelů.
                User user = new User();
                user.setUsername(usernameInput.getText());
                user.setPassword(passwordInput.getText());

                users.add(user);

                // Uložení aktualizovaného seznamu uživatelů do souboru XML
                saveUsers();

                // Zobrazit databázi filmů
                showMovieDatabase(stage);
            }
        });

        VBox registrationForm = new VBox(usernameInput, passwordInput, registerButton);
        registrationForm.setSpacing(10);
        registrationForm.setPadding(new Insets(10));

        // Nastavení scény a zobrazení registračního formuláře
        stage.setScene(new Scene(registrationForm));
        stage.show();
    }






}
