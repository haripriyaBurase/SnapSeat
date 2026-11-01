package com.example.snapSeat.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
public class TestController {
    private final DataSource ds;
    public TestController(DataSource ds){
        this.ds=ds;
    }

    @GetMapping("/api/test")
    public String testDB() throws Exception{
        try(Connection con=ds.getConnection()){
            return "Database Connected: "+
            con.getMetaData().getDatabaseProductName();
        }
    }
}
