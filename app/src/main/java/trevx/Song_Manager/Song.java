package trevx.Song_Manager;

import android.os.Parcel;

/**
 * Created by ptk on 4/25/16.
 */

public class Song {
    public String title;
    public String id;
    public String Image;
    public String link;

    public double last_play_duration;

    public Song() {
    }

    public Song(String title, String id, String Image, String link) {
        this.title = title;
        this.id = id;
        this.Image = Image;
        this.link = link;

    }

    protected Song(Parcel in) {
        title = in.readString();
        id = in.readString();
        Image = in.readString();
        link = in.readString();
        last_play_duration = in.readDouble();
    }



    public double getLast_play_duration() {
        return last_play_duration;
    }

    public void setLast_play_duration(double last_play_duration) {
        this.last_play_duration = last_play_duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String Image) {
        this.Image = Image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "Song{" + "title=" + title + ", id=" + id + ", Image=" + Image + ", link=" + link + '}';
    }

}
