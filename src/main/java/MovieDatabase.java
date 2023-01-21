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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private static final File REVIEW_FILE = new File("src/main/resources/review.xml");


    // list filmů a uživatelů
    private List<Movie> movies = new ArrayList<>();

    private List<User> users = new ArrayList<>();

    private List<Review> reviews = new ArrayList<>();

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
        loadReviews();
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
        Button guestButton = new Button("Pokračovat jako host");
        guestButton.setOnAction(event -> {
            User guest = new User("guest","guest");
            currentUser = guest;
            showDemoDatabase(stage);

        });

        Button registerButton = new Button("Registrace");
        registerButton.setOnAction(event -> showRegistrationForm(stage));

        Image image = new Image("file:src/main/resources/logo.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(150);
        imageView.setFitHeight(50);

        Label label = new Label("Přihlásit se");
        label.setStyle("-fx-font-weight: bold;");
        VBox loginForm = new VBox(label,imageView,usernameInput, passwordInput, loginButton, registerButton, guestButton);

        loginForm.setSpacing(10);
        loginForm.setPadding(new Insets(10));
        loginForm.setAlignment(Pos.CENTER);

        // Nastavení scény a zobrazení přihlašovacího formuláře
        stage.setScene(new Scene(loginForm));
        stage.getIcons().add(image);
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
        // Tlacitko pro zobrazeni uzivatelskeho panelu
        Button userPanelButton = new Button("Zobrazit panel uzivatele");
        userPanelButton.setOnAction(event -> {
            try {
                showUserPanelStage(stage);
            } catch (IOException e) {
                throw new RuntimeException(e);
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

        VBox container = new VBox(movieTable, searchBar, addMovieForm, logoutButton, userPanelButton);
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
        Schema schema = factory.newSchema(new StreamSource(Objects.requireNonNull(getClass().getResource("/movies.xsd")).getFile()));
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

    private void loadReviews() {
        XmlMapper xmlMapper = new XmlMapper();
        try {
            reviews = xmlMapper.readValue(REVIEW_FILE, xmlMapper.getTypeFactory().constructCollectionType(List.class, Review.class));
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
    // Uživatelský panel, sloužící pro nastavení účtu.
    private void showUserPanelStage (Stage stage) throws IOException {

        // Zavedeni okna, uvitani uzivatele, vykresleni prvku
        BorderPane borderPaneUserPanel = new BorderPane();
        Scene userScene = new Scene(borderPaneUserPanel);
        borderPaneUserPanel.setPrefSize(800,600);
        borderPaneUserPanel.setPadding(new Insets(10));

        //Load obrazku uzivatele, pokud neni zadny obrazek definovany uzivatelem, nacte default obrazek.
        Image imageUser = null;

        try {
            imageUser = new Image(new FileInputStream("src/main/resources/" +currentUser.getUsername()+ ".png"));
        } catch (FileNotFoundException e) {
            imageUser = new Image(Files.newInputStream(Paths.get("src/main/resources/host.png")));
        }
        ImageView usersImage = new ImageView(imageUser);
        usersImage.setFitHeight(150);
        usersImage.setFitWidth(150);



        VBox leftPanelUser = new VBox();
        Label labelUserPanel = new Label("Vítej, " + currentUser.getUsername());
        labelUserPanel.setFont(Font.font("Arial", FontWeight.BOLD, 25));

        //Výpis recenzí pro daného uživatele, pracuje se souborem review.xml, informace o uživateli zjišťuje pomocí lognutého jména.
        TableView<Review> usersReviews = new TableView<>();
        TableColumn<Review, String> reviewColumn = new TableColumn<>("Recenze");
        reviewColumn.setCellValueFactory(new PropertyValueFactory<>("textRecenze"));
        TableColumn<Review, String> userColumn = new TableColumn<>("Uživatel");
        userColumn.setCellValueFactory(new PropertyValueFactory<>("revUser"));
        TableColumn<Review, String> revMovieColumn = new TableColumn<>("Film");
        revMovieColumn.setCellValueFactory(new PropertyValueFactory<>("reviewedMovie"));
        TableColumn<Review, String> ratingColumn = new TableColumn<>("Hodnoceni");
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("revHodnoceni"));
        usersReviews.getColumns().setAll(reviewColumn, userColumn, revMovieColumn, ratingColumn);
        usersReviews.setItems(FXCollections.observableArrayList(reviews));
        String user = currentUser.getUsername();
        List<Review> filteredReviews = reviews.stream().filter(review -> review.getRevUser().contains(user)).collect(Collectors.toList());
        usersReviews.setItems(FXCollections.observableArrayList(filteredReviews));

        //Tlačítko pro změnu hesla
        Button changePasswordButton = new Button("Změna hesla");
        changePasswordButton.setOnAction(event -> changePasswordScene(stage));

        Button goToMainStage = new Button("Zpět do hlavního menu");
        goToMainStage.setOnAction(event -> showMovieDatabase(stage));

        //Tlačítko pro změnu obrázku
        Button changePicture = new Button("Změna profilového obrázku");
        changePicture.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Vyberte si nový profilový obrázek");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Obrázky","*.png"));
            File selectedFile = fileChooser.showOpenDialog(stage);
            String photoName = new String(currentUser.getUsername()+".png");
            if (selectedFile != null) {
                try {
                    // Načtení vybraného souboru do byte array
                    byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                    // Zapsání byte array do resource složky
                    Path resourceFolder = Paths.get("src/main/resources");
                    Files.write(resourceFolder.resolve(photoName), fileContent);
                    showUserPanelStage(stage);

                } catch (IOException e) {
                    Alert alertSomethingIsWrongICanFeelIt = new Alert(Alert.AlertType.ERROR, "Něco se pokazilo, opakuj nahrání obrázku.");
                    alertSomethingIsWrongICanFeelIt.show();
                }
            }
        });




        // Přidání všech elementů na user stage
        leftPanelUser.setPadding(new Insets(10));
        leftPanelUser.getChildren().setAll(usersImage, labelUserPanel, changePasswordButton, goToMainStage, changePicture);
        borderPaneUserPanel.setLeft(leftPanelUser);
        borderPaneUserPanel.setCenter(usersReviews);
        stage.setScene(userScene);

    }
    //Otevře nové okno určené pro změnu hesla
    private void changePasswordScene(Stage stage) {
        BorderPane borderPaneChangePassword = new BorderPane();
        Scene changePasswordScene = new Scene(borderPaneChangePassword);
        borderPaneChangePassword.setPrefSize(400,400);
        borderPaneChangePassword.setPadding(new Insets(10));
        VBox changePasswordVBox = new VBox();
        HBox changePasswordHBox = new HBox();
        Stage changePasswordStage = new Stage();
        changePasswordStage.setScene(changePasswordScene);

        Label changePasswordTopLabel = new Label("Změna hesla");
        changePasswordTopLabel.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        Button changePasswordButtonBack = new Button("Zpět");
        Button changePasswordButtonCommit = new Button("Změnit heslo");
        PasswordField changePasswordOldPass = new PasswordField();
        PasswordField changePasswordNewPass = new PasswordField();
        PasswordField changePasswordNewPassConf = new PasswordField();
        Label changePasswordOldPassLabel = new Label("Staré heslo");
        Label changePasswordNewPassLabel = new Label("Nové heslo");
        Label changePasswordNewPassConfLabel = new Label("Nové heslo znovu");
        changePasswordHBox.getChildren().setAll(changePasswordButtonBack, changePasswordButtonCommit);
        changePasswordVBox.getChildren().setAll(changePasswordTopLabel, changePasswordOldPassLabel, changePasswordOldPass,  changePasswordNewPassLabel, changePasswordNewPass, changePasswordNewPassConfLabel, changePasswordNewPassConf, changePasswordHBox);

        changePasswordButtonBack.setOnAction(event -> changePasswordStage.close());

        changePasswordButtonCommit.setOnAction(event -> {
            if (changePasswordOldPass.getText().equals(currentUser.getPassword())){
                if (changePasswordNewPass.getText().equals(changePasswordNewPassConf.getText())) {
                    if (changePasswordNewPass.getText().equals("")){
                        Alert alertNoPass = new Alert(Alert.AlertType.ERROR, "Nové heslo nesmí být prázdné.");
                        alertNoPass.show();

                    } else {
                        User user = currentUser;
                        user.setUsername(currentUser.getUsername());
                        user.setPassword(changePasswordNewPass.getText());
                        saveUsers();
                        Alert alertNewPass = new Alert(Alert.AlertType.INFORMATION, "Změna hesla provedena úspěšně, přihlas se.");
                        alertNewPass.show();
                        changePasswordStage.close();
                        start(stage);

                    }
                } else {
                    Alert alertNotMatchingPass = new Alert(Alert.AlertType.ERROR, "Nová hesla se neshodují.");
                    alertNotMatchingPass.show();
                }
            } else {
                Alert alertWrongOldPass = new Alert(Alert.AlertType.ERROR, "Staré heslo se neshoduje.");
                alertWrongOldPass.show();}
        });

        borderPaneChangePassword.setCenter(changePasswordVBox);
        changePasswordStage.initModality(Modality.APPLICATION_MODAL);
        changePasswordStage.show();

    }

    private void showDemoDatabase(Stage stage) {
        //Ukazka demo databaze pro hosta, ukazuji se pouze seznamy filmu a je zde moznost vyhledavani
        // Vytvoření tabulky filmů
        BorderPane demoBorderPane = new BorderPane();
        Scene demoScene = new Scene(demoBorderPane);
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
        Button logoutButton = new Button("Odhlásit se z účtu Host");
        logoutButton.setOnAction(event -> {
            currentUser = null;

            start(stage);
        });
        VBox container = new VBox(movieTable, searchBar, logoutButton);
        container.setSpacing(10);
        container.setPadding(new Insets(10));

        demoBorderPane.setCenter(container);
        stage.setScene(demoScene);
        stage.show();
    }
    private void showRegistrationForm(Stage stage) {
        // Vytvoření registračního formuláře
        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Username");

        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Heslo");


        Button registerButton = new Button("Registrace");
        registerButton.setOnAction(event -> {

            boolean usernameTaken = users.stream()
                    .anyMatch(u -> u.getUsername().equals(usernameInput.getText()));
            //  Kontrola uzivatelskeho jmena, zda neni prazdne
            if(usernameInput.getText().equals("")) {
                Alert alertNothing = new Alert(Alert.AlertType.ERROR, "Uživatelské jméno nesmí být prázdné.");
                alertNothing.show();
            } else {
                //  Zkontrolujte, zda je zadané uživatelské jméno již obsazeno.
                if (passwordInput.getText().equals("")){
                    Alert alertNoPass = new Alert(Alert.AlertType.ERROR, "Uživatelské heslo nesmí být prázdné.");
                    alertNoPass.show();
                } else {
                    if (usernameTaken) {
                // Pokud je uživatelské jméno obsazeno, zobrazí se chybová zpráva.
                Alert alert = new Alert(Alert.AlertType.ERROR, "Uživatelské jméno je již obsazeno.");
                alert.show();
                } else {
                // Pokud uživatelské jméno není obsazeno, přidejte nového uživatele do seznamu uživatelů.
                User user = new User();
                user.setUsername(usernameInput.getText());
                user.setPassword(passwordInput.getText());

                users.add(user);

                // Uložení aktualizovaného seznamu uživatelů do souboru XML
                saveUsers();
                //Zpráva o úspěšné registraci
                        Alert alertRegistrationComplete = new Alert(Alert.AlertType.INFORMATION, "Registrace byla úspěšná, přihlas se pomocí svých zadaných údajů.");
                        alertRegistrationComplete.show();
                // Zobrazení login obrazovky
                start(stage);
            }}}
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