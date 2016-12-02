package core;

public interface Loger {
	public void log(String txt, boolean addCR);
	public void warn(String txt, boolean addCR);
	public void error(String txt, boolean addCR);
	public void success(String txt, boolean addCR);
}
