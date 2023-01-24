import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;


@JacksonXmlRootElement(localName = "review")
public class Review {
    @JacksonXmlProperty(localName = "reviewedMovie")
    private String reviewedMovie;

    @JacksonXmlProperty(localName = "revUser")
    private String revUser;

    @JacksonXmlProperty(localName = "textRecenze")

    private String textRecenze;
    @JacksonXmlProperty(localName = "revHodnoceni")
    @JsonProperty(required = false)
    private float revHodnoceni;


    // NEMAZAT!!
    public Review() {
        // Výchozí konstruktor, který Jackson potřebuje pro deserializaci

    }

    public Review(String reviewedMovie, String revUser, String textRecenze, float revHodnoceni) {
        this.reviewedMovie = reviewedMovie;
        this.revUser = revUser;
        this.textRecenze = textRecenze;
        this.revHodnoceni = revHodnoceni;
    }


    public String getReviewedMovie() {
        return reviewedMovie;
    }

    public void setReviewedMovie(String reviewedMovie) {
        this.reviewedMovie = reviewedMovie;
    }

    public String getRevUser() {
        return revUser;
    }

    public void setRevUser(String revUser) {
        this.revUser = revUser;
    }

    public String getTextRecenze() {return textRecenze;}

    public void setTextRecenze(String textRecenze) {this.textRecenze=textRecenze;}
    public float getRevHodnoceni() {return revHodnoceni; }

    public void setRevHodnoceni(float revHodnoceni) {this.revHodnoceni = revHodnoceni;}

    @Override
    public String toString() {
        return textRecenze;
    }

}
