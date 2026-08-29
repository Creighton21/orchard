package com.creightoncornelison;
import com.creightoncornelison.messaging.RabbitMqConnection;
import com.creightoncornelison.persistence.Database;

public class Main {
    static void main() throws Exception {
        System.out.println("Orchard starting...");

        Database database = new Database();

        RabbitMqConnection.publish();

        RabbitMqConnection.consume();
    }
}
