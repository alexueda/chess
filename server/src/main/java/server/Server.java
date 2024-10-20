package server;

import spark.*;
import handler.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        // Initialize all handler instances
        ClearHandler clearHandler = new ClearHandler();
        RegisterHandler registerHandler = new RegisterHandler();

        // use spark web flame to create endpoint setup
        Spark.delete("/db", (req, res) -> clearHandler.handleClear(req, res)); // Clear database
        Spark.post("/user", (req, res) -> registerHandler.handleRegister(req, res)); // Register user

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
