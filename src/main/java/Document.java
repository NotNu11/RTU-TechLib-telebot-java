public class Document implements Item{
    private String id;
    private String name;
    private String path;
    private boolean onCloud = true;

    public Document(String id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
        if (path.startsWith("/media")){
            this.onCloud = false;
        }
    }

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
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
}
