package prismsoul.util.safecopy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copies a file from an unreliable source (that goes offline from time to time, such as an overheating harddisk) to a safe source.
 * Whenever the source becomes unresponsive, the program waits for 10 seconds and retries until the source becomes available again.
 * Saved a couple of files of mine.
 * @author Prismsoul
 */
public class SafeCopy
{

	public static void main(String[] args)
	{
		File source = new File(args[0]);
		File destination = new File(args[1]);
		if (!destination.exists())
			destination.mkdirs();
		destination = new File(destination.getAbsolutePath() + "/" + source.getName());
		System.out.println("Copying " + source.getAbsolutePath() + " to " + destination.getAbsolutePath());
		int lastpercent = 0;
		long totalsize = source.length();
		InputStream in = null;
		try
		{
			in = new FileInputStream(source);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		try
		{
			OutputStream out = null;
			try
			{
				out = new FileOutputStream(destination);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
			try
			{
				// Transfer bytes from in to out
				byte[] buf = new byte[65536];
				int len = 0;
				long position = 0;
				while (true)
				{
					boolean readok = false;
					while (!readok)
					{
						try
						{
							len = in.read(buf);
							readok = true;
						}
						catch (IOException e)
						{
							//e.printStackTrace();
							// Could not read
							System.err.println("Could not read... at position " + position + " waiting and retry");
							try
							{
								Thread.sleep(10000);
							}
							catch (InterruptedException e1)
							{
								e1.printStackTrace();
							}
							try
							{
								in.close();
							}
							catch (IOException e3)
							{
								// Whatever!
							}
							in = null;
							while (in == null)
							{
								try
								{
									in = new FileInputStream(source);
								}
								catch (FileNotFoundException fnfe)
								{
									in = null;
									// Could not find the file, wait!
									System.err.println("Could not find file... waiting...");
									try
									{
										Thread.sleep(10000);
									}
									catch (InterruptedException e1)
									{
										e1.printStackTrace();
									}
									continue;
								}
								try
								{
									in.skip(position);
								}
								catch (IOException e1)
								{
									System.err.println("Could not skip to position " + position + "... waiting and retrying");
									try
									{
										in.close();
									}
									catch (IOException e3)
									{
										// whatever
									}
									in = null;
									try
									{
										Thread.sleep(10000);
									}
									catch (InterruptedException e2)
									{
										e2.printStackTrace();
									}
								}
							}
						}
					}

					if (len > 0)
						try
						{
							position += len;
							out.write(buf, 0, len);
							int curpercent = (int)(100.0 * position / totalsize);
							while (curpercent > lastpercent)
							{
								System.out.print(lastpercent % 10 == 0 ? "" + (lastpercent / 10) : ".");
								lastpercent++;
							}
						}
						catch (IOException e)
						{
							e.printStackTrace();
							// Could not write destination, abort!
							System.exit(1);
						}
					else
						break;
				}
			}
			finally
			{
				try
				{
					out.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				// Whatever
			}
		}
		System.out.println("Done!");
	}

}
