package info.guardianproject.pixelknot.utils;

public interface PixelKnotNotificationListener {
	public void init(int num_steps);
	public void update(int additional_steps);
	public void post();
	public void fail(String with_message);
	public void finish();
	public void finish(String result_text);
}
