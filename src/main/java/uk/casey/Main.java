package uk.casey;

import uk.casey.request.PaymentController;

public class Main {
    public static void main(String[] args) throws Exception{
     new PaymentController();
     System.out.println("Started server on http://localhost:8080/payment");
    }
}