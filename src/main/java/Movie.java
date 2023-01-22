import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty(required = false)
    private float hodnoceni;
    @JacksonXmlProperty(localName = "info")
    @JsonProperty(required = false)
    private String info;

    // NEMAZAT!!
    public Movie() {
        // Výchozí konstruktor, který Jackson potřebuje pro deserializaci

    }

    public Movie(String title, int year, String director, float hodnoceni, String info) {
        this.title = title;
        this.year = year;
        this.director = director;
        this.hodnoceni = hodnoceni;
        this.info = info;
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

    public String getInfo() {return info;}
    public void setInfo() {this.info = info;}

}
