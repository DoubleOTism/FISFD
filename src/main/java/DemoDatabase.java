import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

public class DemoDatabase {
    private final Stage stage;
    private final List<Movie> movies;
    private User currentUser;



    public DemoDatabase(Stage stage, List<Movie> movies, User currentUser) throws URISyntaxException {
        this.stage = stage;
        this.movies = movies;
        this.currentUser = currentUser;

    }

    public void show() {
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
            MovieDatabase movieDatabase = null;
            try {
                movieDatabase = new MovieDatabase();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            movieDatabase.start(stage);
        });
        VBox container = new VBox(movieTable, searchBar, logoutButton);
        container.setSpacing(10);
        container.setPadding(new Insets(10));

        demoBorderPane.setCenter(container);
        stage.setScene(demoScene);
        stage.show();
    }
}

