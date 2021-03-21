package config;

import java.io.InputStream;

public class Resource {
    private String ResourceType;
    private Integer resourceNum;

    public Resource(String type,int num){
        this.ResourceType = type;
        this.resourceNum = num;
    }
    public String getResourceType() {
        return ResourceType;
    }

    public void setResourceType(String resourceType) {
        ResourceType = resourceType;
    }

    public int getResourceNum() {
        return resourceNum;
    }

    public void setResourceNum(int resourceNum) {
        this.resourceNum = resourceNum;
    }
}

