import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;


@JacksonXmlRootElement(localName = "movie")
public class Movie {
    @JacksonXmlProperty(localName = "title")
    private String title;

    @JacksonXmlProperty(localName = "year")
    private int year;

    @JacksonXmlProperty(localName = "director")

    private String director;
    @JacksonXmlProperty(localName = "rating")
    private float hodnoceni;


    // NEMAZAT!!
    public Movie() {
        // Výchozí konstruktor, který Jackson potřebuje pro deserializaci

    }

    public Movie(String title, int year, String director, float hodnoceni) {
        this.title = title;
        this.year = year;
        this.director = director;
        this.hodnoceni = hodnoceni;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public float getHodnoceni() {return hodnoceni;}

    public void setHodnoceni(float hodnoceni) {this.hodnoceni=hodnoceni;}
    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;

    }

}
