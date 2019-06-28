import java.util.ArrayList;

public class Book implements Item{
    private String id;
    private String name;
    private String path;
    private ArrayList<String> categories;
    private boolean onCloud = true;

    public Book(String id, String name, String path, ArrayList<String> categories) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.categories = categories;
        if (path.startsWith("/media")){
            this.onCloud = false;
        }
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", categories=" + categories +
                ", onCloud=" + onCloud +
                '}';
    }

    public boolean isOnCloud() {
        return onCloud;
    }

    public void setOnCloud(boolean onCloud) {
        this.onCloud = onCloud;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

}
