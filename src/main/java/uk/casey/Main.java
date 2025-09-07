package uk.casey;

import uk.casey.request.AggregateController;

public class Main {
    public static void main(String[] args) throws Exception{
     new AggregateController();
     System.out.println("Started server on http://localhost:8080");
    }
}