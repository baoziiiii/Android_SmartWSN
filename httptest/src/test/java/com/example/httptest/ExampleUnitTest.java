package com.example.httptest;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        String json="{\n" +
                "\"students\": [\n" +
                "{ \"firstName\":\"John\" , \"lastName\":\"Doe\" },\n" +
                "{ \"firstName\":\"Anna\" , \"lastName\":\"Smith\" },\n" +
                "{ \"firstName\":\"Peter\" , \"lastName\":\"Jones\" }\n" +
                "]\n" +
                "}";

        Gson gson=new Gson();
        Students students=new Students();
        Students.Student student1=new Students.Student();
        student1.setFirstName("aaa");
        student1.setLastName("bbb");
        Students.Student student2=new Students.Student();
        student2.setFirstName("aaa");
        student2.setLastName("bbb");
        students.setStudents(new Students.Student[]{student1,student2});
        System.out.println(gson.toJson(students));
    }
}