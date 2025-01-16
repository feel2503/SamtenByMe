package kr.co.thiscat.samtenbyme;

public class WebPageItem
{
    private Webpage url1;
    private Webpage url2;

    public Webpage getUrl1() {
        return url1;
    }

    public void setUrl1(Webpage url1) {
        this.url1 = url1;
    }

    public Webpage getUrl2() {
        return url2;
    }

    public void setUrl2(Webpage url2) {
        this.url2 = url2;
    }
}

class Webpage
{
    String url;
    String position;
    int width;
    int height;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}