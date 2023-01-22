import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.xml.sax.SAXException;
import javafx.scene.image.Image ;

import javax.swing.text.LabelView;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import java.net.URISyntaxException;
import java.net.URL;
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
    private boolean pridani;

    public MovieDatabase() throws URISyntaxException {
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // načte filmy a uživatele z XML souborů
        loadMovies();
        loadUsers();

showLoginForm(stage);
        stage.show();



    }


    // vytvoření showLoginForm pro login uživatelů
    private void showLoginForm(Stage stage) {
        // Create login form elements
        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Username");

        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Heslo");
         /*
        Metoda setOnAction tlačítka loginButton slouží k určení event handler, která se zavolá po kliknutí na tlačítko.
        Event handler zkontroluje, zda jsou zadané uživatelské jméno a heslo správné, a to tak, že filtruje seznam uživatelů a
        vyhledá uživatele se zadaným uživatelským jménem a heslem. Pokud je uživatel nalezen, je pole currentUser nastaveno na
        tohoto uživatele a zobrazí se databáze filmů. Pokud není nalezen žádný uživatel, zobrazí se chybová zpráva.
        Uživatel se tak může přihlásit do aplikace a získat přístup k databázi filmů.
         */

        Button loginButton = new Button("Přihlášení");
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

        Image image = new Image("file:src/main/resources/logo.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(150);
        imageView.setFitHeight(50);

        Rectangle rectangle = new Rectangle(200, 200);



        Label label = new Label("Přihlásit se");
        label.setStyle("-fx-font-weight: bold;");
        VBox loginForm = new VBox(label,imageView,usernameInput, passwordInput, loginButton, registerButton, rectangle);

        loginForm.setSpacing(10);
        loginForm.setPadding(new Insets(10));
        loginForm.setAlignment(Pos.CENTER);

        // Nastavení scény a zobrazení přihlašovacího formuláře
        stage.setScene(new Scene(loginForm));
        stage.getIcons().add(image);
        stage.show();
    }


    private void showMovieDatabase(Stage stage) {


        // hlavni okno
        VBox root = new VBox();
        root.setPrefHeight(400);
        root.setPrefWidth(640);

// Anchor pane
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setMaxHeight(-1);
        anchorPane.setMaxWidth(-1);
        anchorPane.setPrefHeight(-1);
        anchorPane.setPrefWidth(-1);

// Label Moje filmy
        Label mojeFilmy = new Label();
        mojeFilmy.setAlignment(Pos.CENTER);
        mojeFilmy.setLayoutX(369);
        mojeFilmy.setLayoutY(40);
        mojeFilmy.setPrefHeight(30);
        mojeFilmy.setPrefWidth(101);
        mojeFilmy.setText("Moje filmy");
        mojeFilmy.setTextAlignment(TextAlignment.CENTER);
        mojeFilmy.setWrapText(false);
        mojeFilmy.setFont(new Font(20));
        mojeFilmy.setTextFill(Color.web("#ba0305"));

// moje filmy line
        Line mojeFilmyLine = new Line();
        mojeFilmyLine.setEndX(65);
        mojeFilmyLine.setLayoutX(229);
        mojeFilmyLine.setLayoutY(71);
        mojeFilmyLine.setStartX(-76);
        mojeFilmyLine.setStroke(Color.web("#ba0305"));

// hlavni strana line
        Line hlavniStranaLine = new Line();
        hlavniStranaLine.setEndX(70);
        hlavniStranaLine.setLayoutX(408);
        hlavniStranaLine.setLayoutY(70);
        hlavniStranaLine.setStartX(-45);

// hlavni strana label
        Label hlavniStrana = new Label();
        hlavniStrana.setAlignment(Pos.CENTER);
        hlavniStrana.setLayoutX(164);
        hlavniStrana.setLayoutY(41);
        hlavniStrana.setPrefHeight(30);
        hlavniStrana.setPrefWidth(123);
        hlavniStrana.setText("Hlavní strana");
        hlavniStrana.setTextAlignment(TextAlignment.CENTER);
        hlavniStrana.setWrapText(false);
        hlavniStrana.setFont(new Font(20));

// top bar rectangle
        Rectangle topBarRect = new Rectangle();
        topBarRect.setArcHeight(5);
        topBarRect.setArcWidth(5);
        topBarRect.setFill(Color.web("#ba0305"));
        topBarRect.setHeight(38);
        topBarRect.setLayoutX(3);
        topBarRect.setLayoutY(3);
        topBarRect.setStroke(Color.web("#837272"));
        topBarRect.setStrokeType(StrokeType.INSIDE);
        topBarRect.setStrokeWidth(0);
        topBarRect.setWidth(634);
//search Field

        TextField searchField = new TextField();
        searchField.setLayoutX(104);
        searchField.setLayoutY(9);
        searchField.setPrefHeight(25);
        searchField.setPrefWidth(144);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(16);
        imageView.setFitWidth(16);
        imageView.setLayoutX(614);
        imageView.setLayoutY(14);

        Image profileIcon = new Image(getClass().getResourceAsStream("src/main/resources/profile.png"));
        imageView.setImage(profileIcon);

        Rectangle sideMenu = new Rectangle();
        sideMenu.setArcHeight(5);
        sideMenu.setArcWidth(5);
        sideMenu.setFill(Color.web("#ba0305"));
        sideMenu.setHeight(76);
        sideMenu.setLayoutX(516);
        sideMenu.setLayoutY(6);
        sideMenu.setStroke(Color.BLACK);
        sideMenu.setStrokeType(StrokeType.INSIDE);
        sideMenu.setWidth(119);

        Label mujProfil = new Label();
        mujProfil.setLayoutX(528);
        mujProfil.setLayoutY(32);
        mujProfil.setText("Můj profil");
        mujProfil.setTextFill(Color.WHITE);

        Label odhlasit = new Label();
        odhlasit.setLayoutX(528);
        odhlasit.setLayoutY(57);
        odhlasit.setText("Odhlásit");
        odhlasit.setTextFill(Color.WHITE);

        HBox hbox = new HBox();
        hbox.setLayoutX(67);
        hbox.setLayoutY(96);
        hbox.setPrefHeight(100);
        hbox.setPrefWidth(502);

        ImageView xButton = new ImageView();
        xButton.setFitHeight(21);
        xButton.setFitWidth(21);
        xButton.setLayoutX(611);
        xButton.setLayoutY(8);
        Image image1 = new Image(getClass().getResourceAsStream("xButton.png"));
        xButton.setImage(image1);

        ImageView searchIcon = new ImageView();
        searchIcon.setFitHeight(14);
        searchIcon.setFitWidth(14);
        searchIcon.setLayoutX(113);
        searchIcon.setLayoutY(15);
        Image image2 = new Image(getClass().getResourceAsStream("searchIcon.png"));
        searchIcon.setImage(image2);

        ImageView profile = new ImageView();
        profile.setFitHeight(16);
        profile.setFitWidth(16);
        profile.setLayoutX(614);
        profile.setLayoutY(32);
        Image image3 = new Image(getClass().getResourceAsStream("profile.png"));
        profile.setImage(image3);

        ImageView logout = new ImageView();
        logout.setFitHeight(20);
        logout.setFitWidth(20);
        logout.setLayoutX(612);
        logout.setLayoutY(55);
        Image image4 = new Image(getClass().getResourceAsStream("logout.png"));
        logout.setImage(image4);



        // Vytvoření tabulky filmů
        TableView<Movie> movieTable = new TableView<>();

        TableColumn<Movie, String> titleColumn = new TableColumn<>("Název");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Movie, Integer> yearColumn = new TableColumn<>("Rok");
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<Movie, String> directorColumn = new TableColumn<>("Režisér");
        directorColumn.setCellValueFactory(new PropertyValueFactory<>("director"));

        TableColumn<Movie, Integer> ratingColumn = new TableColumn<>("Hodnocení");
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("hodnoceni"));

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
                            Float.toString(movie.getHodnoceni()).contains(newValue) ||
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

        // Vytvoření ChoiceBox pro volbu, zda má být film ohodnocen
        ChoiceBox<String> hasRatingChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList("Yes", "No"));
        hasRatingChoiceBox.getSelectionModel().select(1);

        TextField hodnoceniInput = new TextField();
        hodnoceniInput.setPromptText("Hodnoceni");

        Button addButton = new Button("Add Movie");
        addButton.setOnAction(event -> {
            // Vytvoření nového filmu se vstupními hodnotami
            // Získání hodnocení filmu
            String hasRating = hasRatingChoiceBox.getSelectionModel().getSelectedItem();
            float rating = 0;
            if (hasRating.equals("Yes")) {
                rating = Float.parseFloat(hodnoceniInput.getText());
            }
            Movie movie = new Movie(titleInput.getText(), Integer.parseInt(yearInput.getText()), directorInput.getText(), Float.parseFloat(hodnoceniInput.getText()));

            try {
                validateMovieAgainstXsd(movie);
                // Přidání filmu do seznamu filmů
                movies.add(movie);
                // Vymazání vstupních polí
                titleInput.clear();
                yearInput.clear();
                directorInput.clear();
                hodnoceniInput.clear();
                // Aktualizujte tabulku tak, aby zobrazovala nově přidaný film
                movieTable.setItems(FXCollections.observableArrayList(movies));
            } catch (SAXException | IOException | URISyntaxException e) {
                // Zobrazte chybové hlášení
                Alert alert = new Alert(Alert.AlertType.ERROR, "Film se nepodařilo přidat do databáze: " + e.getMessage());
                alert.show();
            }
        });

        // Tlačítko pro odebrání filmu
        Button deleteButton = new Button("Odstranit");
        deleteButton.setOnAction(event -> {
            Movie vybranejFilm = movieTable.getSelectionModel().getSelectedItem();

            if (vybranejFilm != null) {
                /*confirmation box pro overeni odstraneni filmu po ok se vybrany film odstrani z movies a nasledne se zavola metoda deleteMovies() ktera zmenu zapise do XML souboru
                a nasledne metoda loadMovies() ktera nacte zmenu ,
                nasledne se updatne movieTable (table se vseme filmama) aby odpovidal aktualnimu stavu XML.
                (todo: hodit to do jedny metody;)
                 */
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Jste si jisti, že chcete tento film odstranit?");
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        System.out.println("Odstranit");
                        movies.remove(vybranejFilm);
                        deleteMovies();
                        loadMovies();
                        movieTable.setItems(FXCollections.observableArrayList(movies));
                    }
                });
            }
        });

        HBox addMovieForm = new HBox(titleInput, yearInput, directorInput,hasRatingChoiceBox, hodnoceniInput,addButton, deleteButton);
        addMovieForm.setSpacing(10);


        // Vytvoření tlačítka pro odhlášení
        Button logoutButton = new Button("Odhlášení");
        logoutButton.setOnAction(event -> {
            // Vymazání aktuálního uživatele a zobrazení přihlašovacího formuláře
            currentUser = null;

            start(stage);
        });

        VBox container = new VBox(movieTable, searchBar, addMovieForm, logoutButton);
        container.setSpacing(10);
        container.setPadding(new Insets(10));

        // Nastavení scény a zobrazení filmové databáze
        stage.setScene(new Scene(container));
        stage.show();


    }


    // validace proti XSD souboru
    private void validateMovieAgainstXsd(Movie movie) throws SAXException, IOException, URISyntaxException {
        // Vytovreni schema factory co vytvori schema z XSD souboru
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(getClass().getResource("/movies.xsd").getFile()));
        //Schema schema = factory.newSchema(new StreamSource(new File("src/main/resources/movies.xsd")));
        Validator validator = schema.newValidator();

        // vytvoreni xmlmapper pro prevedeni Movie na XML string (bez pretvoreni na string to nejde)
        XmlMapper xmlMapper = new XmlMapper();
        String xml = xmlMapper.writeValueAsString(movie);

        // valiadce nove vytvoreneho XML stringu vuci nasemu schematu
        validator.validate(new StreamSource(new StringReader(xml)));

        // nacteni existujiciho xml souboru (jinak se bude zapisovat pouze jeden film)
        List<Movie> existingMovies = xmlMapper.readValue(MOVIES_FILE, xmlMapper.getTypeFactory().constructCollectionType(List.class, Movie.class));
        // pridani noveho filmu do listu
        existingMovies.add(movie);

        // zapsani noveho filmu do XML
        xmlMapper.writeValue(MOVIES_FILE, existingMovies);
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


    private void deleteMovies() {
        XmlMapper xmlMapper = new XmlMapper();
        try {
            // Write the updated list back to the file

            xmlMapper.writeValue(MOVIES_FILE, movies);
            // Reload the movies list

        } catch (IOException e) {
            System.out.println("Error deleting movie from file: " + e.getMessage());
        }
    }



    private void saveMovies() {
        XmlMapper xmlMapper = new XmlMapper();
        try {
            xmlMapper.writeValue(MOVIES_FILE, movies);
            movies = xmlMapper.readValue(MOVIES_FILE, xmlMapper.getTypeFactory().constructCollectionType(List.class, Movie.class));

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
        // Tlačitko na vrácení do login formuláře
        Button backButton = new Button("Back");
        backButton.setOnAction(event -> {
            // Create a new scene for the login form
            showLoginForm(stage);
        });

        Image image = new Image("file:src/main/resources/logo.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(150);
        imageView.setFitHeight(50);

        Label label = new Label("Registrovat se");
        label.setStyle("-fx-font-weight: bold;");

        VBox registrationForm = new VBox(label,imageView,usernameInput, passwordInput, registerButton, backButton);
        registrationForm.setSpacing(10);
        registrationForm.setPadding(new Insets(10));
        registrationForm.setAlignment(Pos.CENTER);


        // Nastavení scény a zobrazení registračního formuláře
        stage.setScene(new Scene(registrationForm));
        stage.getIcons().add(image);
        stage.show();
    }






}
