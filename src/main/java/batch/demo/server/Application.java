package batch.demo.server;

public class Application {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		int port = Integer.parseInt(args[0]);
		WebServer server = new WebServer(port);
		try {
			server.start();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			server.stop();
		}
	}

}
