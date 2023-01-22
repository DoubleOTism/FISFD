import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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


    private Movie currentMovie;
    private Movie vybranejFilm;
    TableView<Movie> movieTable = new TableView<>();


    private String currentMovieString;

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
            DemoDatabase demo = null;
            try {
                demo = new DemoDatabase(stage, movies, currentUser);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            demo.show();

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


        // hlavni okno
        VBox root = new VBox();
        root.setPrefHeight(640);
        root.setPrefWidth(800);

// Anchor pane
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setMaxHeight(-1);
        anchorPane.setMaxWidth(-1);
        anchorPane.setPrefHeight(-1);
        anchorPane.setPrefWidth(-1);

// Label Moje filmy
        Button mojeFilmy = new Button();
        mojeFilmy.setAlignment(Pos.CENTER);
        mojeFilmy.setLayoutX(495);
        mojeFilmy.setLayoutY(50);
        mojeFilmy.setPrefHeight(30);
        mojeFilmy.setText("Moje filmy");
        mojeFilmy.setTextAlignment(TextAlignment.CENTER);
        mojeFilmy.setBackground(new Background(new BackgroundFill(Color.web("#f7f7f7"), null, null)));
        mojeFilmy.setWrapText(false);
        mojeFilmy.setFont(new Font(20));


        Line hlavniLine = new Line();
        hlavniLine.setEndX(80);
        hlavniLine.setLayoutX(255);
        hlavniLine.setLayoutY(95);
        hlavniLine.setStartX(-60);
       hlavniLine.setStroke(Color.web("#ba0305"));



        Line mojeFilmyLine = new Line();
        mojeFilmyLine.setEndX(65);
        mojeFilmyLine.setLayoutX(555);
        mojeFilmyLine.setLayoutY(95);
        mojeFilmyLine.setStartX(-76);
// hlavni strana label
        Button hlavniStrana = new Button();
        hlavniStrana.setAlignment(Pos.CENTER);
        hlavniStrana.setLayoutX(195);
        hlavniStrana.setLayoutY(50);
        hlavniStrana.setPrefHeight(22);
        hlavniStrana.setText("Hlavní strana");
        hlavniStrana.setTextAlignment(TextAlignment.CENTER);
        hlavniStrana.setWrapText(false);
        hlavniStrana.setFont(new Font(20));
        hlavniStrana.setBackground(new Background(new BackgroundFill(Color.web("#f7f7f7"), null, null)));
        hlavniStrana.setTextFill(Color.web("#ba0305"));

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
        topBarRect.setWidth(794);
//search Field

        TextField searchField = new TextField();
        searchField.setLayoutX(104);
        searchField.setLayoutY(9);
        searchField.setPrefHeight(25);
        searchField.setPrefWidth(144);

        ImageView imageViewoptions = new ImageView();
        imageViewoptions.setFitHeight(16);
        imageViewoptions.setFitWidth(16);
        imageViewoptions.setLayoutX(770);
        imageViewoptions.setLayoutY(14);


        Image optionsIcon = new Image(getClass().getResourceAsStream("src/main/resources/options.png"));
        imageViewoptions.setImage(optionsIcon);
// side menu nahore vpravo, defaultne je neviditelne, po kliknuti ikonky profile zobrazit

        Rectangle sideMenu = new Rectangle();
        sideMenu.setArcHeight(5);
        sideMenu.setArcWidth(5);
        sideMenu.setFill(Color.web("#ba0305"));
        sideMenu.setHeight(76);
        sideMenu.setLayoutX(675);
        sideMenu.setLayoutY(6);
        sideMenu.setStroke(Color.BLACK);
        sideMenu.setStrokeType(StrokeType.INSIDE);
        sideMenu.setWidth(120);

        Label mujProfil = new Label();
        mujProfil.setLayoutX(685);
        mujProfil.setLayoutY(32);
        mujProfil.setText("Můj profil");
        mujProfil.setTextFill(Color.WHITE);


        Label odhlasit = new Label();
        odhlasit.setLayoutX(685);
        odhlasit.setLayoutY(57);
        odhlasit.setText("Odhlásit");
        odhlasit.setTextFill(Color.WHITE);

        HBox xBoxFilmu = new HBox();
        xBoxFilmu.setLayoutX(67);
        xBoxFilmu.setLayoutY(96);
        xBoxFilmu.setPrefHeight(100);
        xBoxFilmu.setPrefWidth(502);

        Button optionsButton = new Button();
        optionsButton.setLayoutX(760);
        optionsButton.setLayoutY(7);
        optionsButton.setGraphic(imageViewoptions);
        optionsButton.setBackground(new Background(new BackgroundFill(Color.web("#ba0305"), null, null)));



        ImageView xButton = new ImageView();
        xButton.setFitHeight(21);
        xButton.setFitWidth(21);
        xButton.setLayoutX(611);
        xButton.setLayoutY(8);
        Image image1 = new Image(getClass().getResourceAsStream("src/main/resources/xButton.png"));
        xButton.setImage(image1);

        ImageView searchIcon = new ImageView();
        searchIcon.setFitHeight(14);
        searchIcon.setFitWidth(14);
        searchIcon.setLayoutX(113);
        searchIcon.setLayoutY(15);
        Image image2 = new Image(getClass().getResourceAsStream("src/main/resources/searchIcon.png"));
        searchIcon.setImage(image2);

        ImageView profile = new ImageView();
        profile.setFitHeight(16);
        profile.setFitWidth(16);
        profile.setLayoutX(614);
        profile.setLayoutY(32);
        Image image3 = new Image(getClass().getResourceAsStream("src/main/resources/profile.png"));
        profile.setImage(image3);


        ImageView logout = new ImageView();
        logout.setFitHeight(20);
        logout.setFitWidth(20);
        logout.setLayoutX(612);
        logout.setLayoutY(55);
        Image image4 = new Image(getClass().getResourceAsStream("src/main/resources/logout.png"));
        logout.setImage(image4);

        Button profileButton = new Button();
        profileButton.setLayoutX(760);
        profileButton.setLayoutY(30);
        profileButton.setGraphic(profile);
        profileButton.setBackground(new Background(new BackgroundFill(Color.web("#ba0305"), null, null)));

        Button odhlasitButton = new Button();
        odhlasitButton.setLayoutX(758);
        odhlasitButton.setLayoutY(50);
        odhlasitButton.setGraphic(logout);
        odhlasitButton.setBackground(new Background(new BackgroundFill(Color.web("#ba0305"), null, null)));


        Group horniMenu = new Group();
        horniMenu.getChildren().addAll(sideMenu, mujProfil, odhlasit, logout, profileButton, odhlasitButton);
        horniMenu.setVisible(false);





        // Vytvoření tabulky filmů

        TableColumn<Movie, String> titleColumn = new TableColumn<>("Název");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Movie, Integer> yearColumn = new TableColumn<>("Rok");
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<Movie, String> directorColumn = new TableColumn<>("Režisér");
        directorColumn.setCellValueFactory(new PropertyValueFactory<>("director"));

        TableColumn<Movie, Integer> ratingColumn = new TableColumn<>("Naše hodnocení");
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


        // Tlacitko pro zobrazeni uzivatelskeho panelu
        profileButton.setOnAction(event -> {
            try {
                showUserPanelStage(stage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        //Přidání prokliknutí na daný film
        movieTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
                String title = selectedMovie.getTitle();
                currentMovie = selectedMovie;
                currentMovieString = title;
                try {
                    showMovieDetails(stage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });

        // Tlačítko pro odebrání filmu
        Button deleteButton = new Button("Odstranit");
        deleteButton.setOnAction(event -> {
            vybranejFilm = movieTable.getSelectionModel().getSelectedItem();

            if (vybranejFilm != null) {

                removeMovieFromDatabase();
            }
        });
        Button addMovieButton = new Button("Přidat film do databáze");
        addMovieButton.setOnAction(event -> {
            addMovieToDatabase(stage);
        });



        // Vytvoření tlačítka pro odhlášení

        odhlasitButton.setOnAction(event -> {
            // Vymazání aktuálního uživatele a zobrazení přihlašovacího formuláře
            currentUser = null;

            start(stage);
        });
        HBox addMovieForm = new HBox(addMovieButton, deleteButton);
        addMovieForm.setSpacing(10);
        VBox container = new VBox(movieTable, searchBar, addMovieForm);
        addMovieForm.setLayoutX(20);
        addMovieForm.setLayoutY(580);
        container.setSpacing(10);
        container.setPadding(new Insets(10));

        // Nastavení scény a zobrazení filmové databáze
        root.getChildren().add(anchorPane);
        anchorPane.getChildren().addAll(mojeFilmy, hlavniLine, mojeFilmyLine, hlavniStrana, topBarRect, searchField, imageViewoptions, xBoxFilmu, searchIcon,  horniMenu, optionsButton, addMovieForm);
        stage.setScene(new Scene(root));
        stage.show();



        // Nastavení scény a zobrazení filmové databáze
        optionsButton.setOnMouseClicked(event -> {
            horniMenu.setVisible(!horniMenu.isVisible());
        });

    }
    /*confirmation box pro overeni odstraneni filmu po ok se vybrany film odstrani z movies a nasledne se zavola metoda deleteMovies() ktera zmenu zapise do XML souboru
                a nasledne metoda loadMovies() ktera nacte zmenu ,
                nasledne se updatne movieTable (table se vseme filmama) aby odpovidal aktualnimu stavu XML.
                 */
    private void removeMovieFromDatabase() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Jste si jisti, že chcete tento film odstranit?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                movies.remove(vybranejFilm);
                deleteMovies();
                loadMovies();
                movieTable.setItems(FXCollections.observableArrayList(movies));
            }
        });
    }
    private void addMovieToDatabase(Stage stage) {
        BorderPane borderPaneAddMovie = new BorderPane();
        borderPaneAddMovie.setMinSize(500,400);
        borderPaneAddMovie.setPadding(new Insets(10));
        Scene addMovieScene = new Scene(borderPaneAddMovie);
        Stage addMovie = new Stage();
        VBox vBox = new VBox();
        HBox hBox = new HBox();
        // Vytvoření formuláře pro přidání filmu
        Label addFilmLabel = new Label("Přidání filmu do databáze");
        addFilmLabel.setFont(Font.font("Arial", FontWeight.BOLD, 25));

        TextField titleInput = new TextField();
        titleInput.setPromptText("Název");

        TextField yearInput = new TextField();
        yearInput.setPromptText("Rok");

        TextField directorInput = new TextField();
        directorInput.setPromptText("Režisér");

        TextArea infoInput = new TextArea();
        infoInput.setPromptText("Info o filmu, jeho obsah");

        Label hodnoceniFilmu = new Label("Bodové hodnocení filmu: ");

        Slider hodnoceniSlider = new Slider(0.0,5.0,0);
        hodnoceniSlider.setBlockIncrement(0.5);
        hodnoceniSlider.setShowTickLabels(true);
        hodnoceniSlider.setSnapToTicks(true);
        hodnoceniSlider.setMajorTickUnit(0.5);
        hodnoceniSlider.setMinorTickCount(0);
        hodnoceniSlider.setShowTickMarks(true);

        Button addButton = new Button("Add Movie");
        addButton.setOnAction(event -> {
            Double doubleToFloat = hodnoceniSlider.getValue();
            Float convertedDouble = doubleToFloat.floatValue();
            // Vytvoření nového filmu se vstupními hodnotami
            try {
                Movie movie = new Movie(titleInput.getText(), Integer.parseInt(yearInput.getText()), directorInput.getText(), convertedDouble, infoInput.getText());
                try {
                    validateMovieAgainstXsd(movie);
                    // Přidání filmu do seznamu filmů
                    movies.add(movie);
                    // Vymazání vstupních polí
                    titleInput.clear();
                    yearInput.clear();
                    directorInput.clear();
                    addMovie.close();
                    showMovieDatabase(stage);
                    System.gc();
                } catch (SAXException | IOException | URISyntaxException | NumberFormatException e) {
                    // Zobrazte chybové hlášení
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Film se nepodařilo přidat do databáze: " + e.getMessage());
                    alert.show();
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Některá z kolonek není vyplněná, zkus to znovu: " + e.getMessage());
                alert.show();
            }
        });
        Button goBackButton = new Button("Zrušit");
        goBackButton.setOnAction(event -> {
            addMovie.close();
            showMovieDatabase(stage);
            System.gc();
        });
        vBox.getChildren().addAll(addFilmLabel, titleInput, yearInput,directorInput, infoInput, hodnoceniFilmu, hodnoceniSlider);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10));
        hBox.getChildren().addAll(addButton, goBackButton);
        borderPaneAddMovie.setCenter(vBox);
        borderPaneAddMovie.setBottom(hBox);
        addMovie.setScene(addMovieScene);
        addMovie.initModality(Modality.APPLICATION_MODAL);
        addMovie.show();
    }


    // validace proti XSD souboru
    private void validateMovieAgainstXsd(Movie movie) throws SAXException, IOException, URISyntaxException {
        // Vytovreni schema factory co vytvori schema z XSD souboru
        Path xsdPath = Paths.get(Objects.requireNonNull(getClass().getResource("src/main/resources/movies.xsd")).toURI());
        if (Files.exists(xsdPath)) {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsdPath.toFile()));
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
    }

    private void validateReviewAgainstXsd(Review review) throws SAXException, IOException, URISyntaxException {
        // Vytovreni schema factory co vytvori schema z XSD souboru
        Path xsdPath = Paths.get(Objects.requireNonNull(getClass().getResource("src/main/resources/review.xsd")).toURI());
        if (Files.exists(xsdPath)) {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsdPath.toFile()));
            Validator validator = schema.newValidator();

        // vytvoreni xmlmapper pro prevedeni Movie na XML string (bez pretvoreni na string to nejde)
        XmlMapper xmlMapper = new XmlMapper();
        String xml = xmlMapper.writeValueAsString(review);

        // valiadce nove vytvoreneho XML stringu vuci nasemu schematu
        validator.validate(new StreamSource(new StringReader(xml)));

        // nacteni existujiciho xml souboru (jinak se bude zapisovat pouze jeden film)
        List<Review> existingReviews = xmlMapper.readValue(REVIEW_FILE, xmlMapper.getTypeFactory().constructCollectionType(List.class, Review.class));
        // pridani noveho filmu do listu
        existingReviews.add(review);

        // zapsani noveho filmu do XML
        xmlMapper.writeValue(REVIEW_FILE, existingReviews);
    }
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
    public void loadUsers() {
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
    public void saveUsers() {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.writeValue(USERS_FILE, users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Slouží pro zobrazení infa o filmu, uživatel dostane image, info filmu, recenze filmu, basic úpdaje o lidech, co to vytvořili.
    public void showMovieDetails (Stage stage) throws IOException {
        BorderPane borderPaneMovieDetail = new BorderPane();
        borderPaneMovieDetail.setMinSize(800,600);
        borderPaneMovieDetail.setPadding(new Insets(10));
        Scene movieScene = new Scene(borderPaneMovieDetail);
        VBox movieDetailsVBox = new VBox();
        VBox movieDetailLeftBox = new VBox();
        HBox movieDetailHBox = new HBox();
        movieDetailsVBox.setMinWidth(800);

        //Load obrazku filmu, pokud neni zadny obrazek definovany uzivatelem, nacte default obrazek.
        Image imageMovie = null;

        try {
            imageMovie = new Image(new FileInputStream("src/main/resources/movies/" + currentMovieString + ".png"));
        } catch (FileNotFoundException e) {
            imageMovie = new Image(Files.newInputStream(Paths.get("src/main/resources/movies/base.png")));
        }
        ImageView movieImage = new ImageView(imageMovie);
        movieImage.setFitHeight(250);
        movieImage.setFitWidth(200);




        // Vytvoření TableView pro recenze u každého filmu, tahá to z XML review.xml, dle jména snímku. Používá .filter
        //  s tím, že si tov ezme název filmu ve stringu a vyfilturje to recenze jen o daném filmu. Retardované, ale
        //  funkční, i guess.
        TableView<Review> movieDetailReview = new TableView<>();
        TableColumn<Review, String> reviewColumn = new TableColumn<>("Recenze");
        reviewColumn.setCellValueFactory(new PropertyValueFactory<>("textRecenze"));
        TableColumn<Review, String> userColumn = new TableColumn<>("Uživatel");
        userColumn.setCellValueFactory(new PropertyValueFactory<>("revUser"));
        TableColumn<Review, String> ratingColumn = new TableColumn<>("Hodnoceni");
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("revHodnoceni"));
        movieDetailReview.getColumns().setAll(reviewColumn, userColumn, ratingColumn);
        movieDetailReview.setItems(FXCollections.observableArrayList(reviews));
        List<Review> filteredReviews = reviews.stream().filter(review -> review.getReviewedMovie().contains(currentMovieString)).collect(Collectors.toList());
        movieDetailReview.setItems(FXCollections.observableArrayList(filteredReviews));
        //Tlačítko pro změnu obrázku filmu, jedno a to samý co u uživatele, akorát jinak pojmenovaný.
        Button changePicture = new Button("\uD83D\uDD04 Změnit obrázek");
        changePicture.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Vyberte nový obrázek pro film");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Obrázky","*.png"));
            File selectedFile = fileChooser.showOpenDialog(stage);
            String photoName = new String(currentMovieString+".png");
            if (selectedFile != null) {
                try {
                    // Načtení vybraného souboru do byte array
                    byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                    // Zapsání byte array do resource složky
                    Path resourceFolder = Paths.get("src/main/resources/movies");
                    Files.write(resourceFolder.resolve(photoName), fileContent);
                    showMovieDetails(stage);

                } catch (IOException e) {
                    Alert alertSomethingIsWrongICanFeelIt = new Alert(Alert.AlertType.ERROR, "Něco se pokazilo, opakuj nahrání obrázku.");
                    alertSomethingIsWrongICanFeelIt.show();
                }
            }
        });

        //Tlacitko na vraceni se na main page z detailů filmu
        Button backFromDetail = new Button("↩ Zpět");
        backFromDetail.setOnAction(event -> {
            showMovieDatabase(stage);
            System.gc();
        });
        //Tlačítko pro vytvoření recenze k filmu
        Button addReview = new Button("➕ Přidat recenzi");
        addReview.setOnAction(event -> {
            showAddReviewStage(stage);
        });


        // Vytvořžení Labelu o informacich filmu, nastavení maximální šířky pro wrapper, nastavení HBoxu a věcí kolem
        //  Je to zmatečný, ale funguje to. Lituji těch, co to budou stylovat.
        Label movieDetailLabel = new Label(currentMovieString);
        movieDetailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        Label director = new Label("Režisér: ");
        Label movieDirector = new Label(currentMovie.getDirector());
        director.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        Label year = new Label("Rok vydání: ");
        year.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        Label movieYear = new Label(Integer.toString(currentMovie.getYear()));
        Label obsahLabel = new Label("Obsah:");
        obsahLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        Label infoLabel = new Label(currentMovie.getInfo());
        infoLabel.setWrapText(true);
        infoLabel.setMaxWidth(750);
        movieDetailLeftBox.getChildren().addAll(movieImage,movieDetailLabel, director, movieDirector, year, movieYear,addReview,changePicture);
        movieDetailLeftBox.setPadding(new Insets(0,10,10,0));
        movieDetailLeftBox.setSpacing(8);
        movieDetailsVBox.getChildren().addAll(obsahLabel,infoLabel, movieDetailReview);
        movieDetailHBox.getChildren().addAll(backFromDetail);
        infoLabel.setPadding(new Insets(0,0,10,0));

        movieDetailHBox.setAlignment(Pos.CENTER);
        movieDetailHBox.setPadding(new Insets(10,0,0,0));
        borderPaneMovieDetail.setLeft(movieDetailLeftBox);
        borderPaneMovieDetail.setCenter(movieDetailsVBox);
        borderPaneMovieDetail.setBottom(movieDetailHBox);
        stage.setScene(movieScene);


    }

    public void showAddReviewStage (Stage stage) {
        BorderPane borderPaneAddReview = new BorderPane();
        borderPaneAddReview.setMinSize(400,300);
        borderPaneAddReview.setPadding(new Insets(10));
        Scene movieScene = new Scene(borderPaneAddReview);
        Stage reviewStage = new Stage();
        VBox reviewVBox = new VBox();
        HBox reviewHBox = new HBox();
        Label labelTop = new Label("Přidat recenzi pro "+ currentMovieString);
        labelTop.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Button goBackButton = new Button("✖ Zrušit");
        goBackButton.setOnAction(event -> {
            reviewStage.close();
        });

        // Vytvoření formuláře pro přidání filmu
        TextArea textRecenzeInput = new TextArea();
        textRecenzeInput.setPromptText("Text vaší recenze");

        Label hodnoceniFilmu = new Label("Bodové hodnocení filmu: ");

        Slider revHodnoceniSlider = new Slider(0.0,5.0,0);
        revHodnoceniSlider.setBlockIncrement(0.5);
        revHodnoceniSlider.setShowTickLabels(true);
        revHodnoceniSlider.setSnapToTicks(true);
        revHodnoceniSlider.setMajorTickUnit(0.5);
        revHodnoceniSlider.setMinorTickCount(0);
        revHodnoceniSlider.setShowTickMarks(true);


        Button addButton = new Button("➕ Přidat recenzi");
        addButton.setOnAction(event -> {
            // Vytvoření nového filmu se vstupními hodnotami
            Double doubleToFloat = revHodnoceniSlider.getValue();
            Float convertedDouble = doubleToFloat.floatValue();

            Review review = new Review(currentMovieString, currentUser.getUsername(), textRecenzeInput.getText(), convertedDouble);

            try {
                validateReviewAgainstXsd(review);
                // Přidání filmu do seznamu filmů
                reviews.add(review);
                // Vymazání vstupních polí
                textRecenzeInput.clear();
                Alert alertCorrect = new Alert(Alert.AlertType.INFORMATION, "Recenzi se podařilo přidat do databáze");
                alertCorrect.show();
                reviewStage.close();
                showMovieDetails(stage);
            } catch (SAXException | IOException | URISyntaxException e) {
                // Zobrazte chybové hlášení
                Alert alert = new Alert(Alert.AlertType.ERROR, "Recenzi se nepodařilo přidat do databáze: " + e.getMessage());
                alert.show();
            }
        });
        textRecenzeInput.setMinSize(360,200);
        hodnoceniFilmu.setPadding(new Insets(5,0,5,0));
        reviewHBox.getChildren().setAll(goBackButton, addButton);
        reviewHBox.setSpacing(8);
        reviewVBox.getChildren().setAll(textRecenzeInput,hodnoceniFilmu, revHodnoceniSlider);

        borderPaneAddReview.setTop(labelTop);
        labelTop.setPadding(new Insets(0,0,10,0));
        labelTop.setAlignment(Pos.CENTER);
        reviewHBox.setAlignment(Pos.CENTER);
        borderPaneAddReview.setCenter(reviewVBox);
        borderPaneAddReview.setBottom(reviewHBox);
        reviewStage.setScene(movieScene);
        reviewStage.initModality(Modality.APPLICATION_MODAL);
        Image image = new Image("file:src/main/resources/logo.png");
        reviewStage.getIcons().add(image);
        reviewStage.show();
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
            imageUser = new Image(new FileInputStream("src/main/resources/users/" + currentUser.getUsername() + ".png"));
        } catch (FileNotFoundException e) {
            imageUser = new Image(Files.newInputStream(Paths.get("src/main/resources/users/host.png")));
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
        changePasswordButton.setOnAction(event -> {
            changePasswordScene(stage);

        });

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
                    Path resourceFolder = Paths.get("src/main/resources/users/");
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
    public User loadCurrenyUser() {
    return currentUser;
    }

}