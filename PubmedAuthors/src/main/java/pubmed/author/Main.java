package pubmed.author;

import java.util.ArrayList;
import java.util.Iterator;

import pubmed.author.sql.SQLConnection;

public class Main {
	public final int LIVE_THREADS = 4;
	private Authors[] authors = new Authors[LIVE_THREADS];
	private ArrayList<String> list;
	private Iterator<String> it;
	private SQLConnection con;

	public static void main(String[] args) {
		try {
			new Main();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Main() throws InterruptedException {
		System.out.println("MAIN THREAD: START");
		con = new SQLConnection("localhost", "3306", "paul", "paul", "[paul3514]");
		con.open();
		list = con.getReferences();
		it = list.iterator();

		run();

		while (!isDone()) {
			Thread.sleep(40);
		}
		con.close();
		System.out.println("MAIN THREAD: DONE");
	}

	private void run() throws InterruptedException {
		while (it.hasNext()) {
			for (Authors auth : authors) {
				if(null == auth || !auth.isDone) {
					auth = new Authors(it.next(), this.con);
					auth.start();
				}
				Thread.sleep(100);
			}
		}
	}

	public boolean isDone() {
		if (it.hasNext())
			return false;
		for (Authors auth : authors) {
			if (!auth.isDone)
				return false;
		}
		return true;
	}
}