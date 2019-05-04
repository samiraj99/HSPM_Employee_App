package com.sam.hspm_employee_app.SignUpForm;

/**
 * Created by Home on 6/10/2017.
 */


public class ImageUploadInfoHelperClass {

    public String imageName;

    public String imageURL;

    public ImageUploadInfoHelperClass() {

    }

    public ImageUploadInfoHelperClass(String name, String url) {

        this.imageName = name;
        this.imageURL= url;
    }
    public ImageUploadInfoHelperClass(String url) {

        this.imageURL= url;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageURL() {
        return imageURL;
    }

}
