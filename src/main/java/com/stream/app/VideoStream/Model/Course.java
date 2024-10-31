package com.stream.app.VideoStream.Model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Course {


    @Id
    private String coursevId;

    private String courseTitle;

//    @OneToMany(mappedBy = "course")
//    List<Video> list=new ArrayList<>();

}
