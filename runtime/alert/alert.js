// Create our alert sender
alerter = new Alerter(monitor, result, config);

// send emails
alerter.send("benc.uk@gmail.com");
