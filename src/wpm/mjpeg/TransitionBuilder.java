/*
 * TransitionBuilder.java
 *
 * Created on May 19, 2007, 11:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package wpm.mjpeg;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import javax.swing.ImageIcon;

/**
 *
 * @author monceaux
 */
public class TransitionBuilder
{
    
    /** Creates a new instance of TransitionBuilder */
    public TransitionBuilder()
    {
    }
    
    public static void main(String[] args) throws Exception
    {
        double framerate = 12.0;
        double transitionDuration = 1; // seconds
        double slideDuration = 3; // seconds
        
        if(args.length == 0)
        {
            System.out.println("TransitionBuilder <dir> <avi file> <framerate> <transition duration> <slide duration>");
            return;
        }
        
        if(args.length > 2)
        {
            try
            {
                switch(args.length)
                {
                    case 5:
                        slideDuration = Double.parseDouble(args[4]);
                    case 4:
                        transitionDuration = Double.parseDouble(args[3]);
                    case 3:
                        framerate = Double.parseDouble(args[2]);
                }
            }
            catch (NumberFormatException ex)
            {
                ex.printStackTrace();
            }
        }
        
        File photoDir = new File(args[0]);
        File[] files = photoDir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                if(name.toLowerCase().endsWith("jpg"))
                    return true;
                return false;
            }
        });
        
        
        
        Arrays.sort(files);
        
        
        
        int numFrames = (int)((files.length) * framerate * (slideDuration + transitionDuration));
        MJPEGGenerator m = new MJPEGGenerator(new File(args[1]), 640, 480, framerate, numFrames);
        
        BufferedImage bim = new BufferedImage(640, 480,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bim.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0,0,640,480);
        for(int i = 0; i < files.length; i++)
        {
            System.out.println("processing file "+files[i].getName());
            ImageIcon ii = new ImageIcon(files[i].getCanonicalPath());
            BufferedImage temp = createImageWithWH(ii.getImage(),640,480);
            
            int numframes = (int)(framerate * transitionDuration);
            for(int x = 0; x < numframes; x++)
            {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f * x / numframes));
                g.drawImage(temp,0,0,null);
                m.addImage(bim);
            }
            
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
            g.drawImage(temp,0,0,null);
            numframes = (int)(framerate * slideDuration);
            for(int x = 0; x < numframes; x++)
            {
                m.addImage(bim);
            }
            
            ii = null;
            temp = null;
        }
        
        m.finishAVI();
    }
    
    public static BufferedImage createImageWithWH(Image image, int width, int height) throws Exception
    {
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        
        int new_w = width;
        int new_h = (int)((1.0 * h * new_w)/(w));
        
        if(new_h < height)
        {
            new_h = height;
            new_w = (int)((1.0 * w * new_h)/(h));
        }
        
        BufferedImage bi = new BufferedImage(new_w,new_h,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.drawImage(image,0,0,new_w,new_h,null);
        
        int ww = (new_w / 2) - (width / 2);
        int hh = (new_h / 2) - (height / 2);
        if(ww < 0)
            ww = 0;
        if(hh < 0)
            hh = 0;
        
        if((ww + width) > new_w)
            ww = 0;
        if((hh + height) > new_h)
            hh = 0;
        return bi.getSubimage(ww,hh,width,height);
    }
    
    public static void renameFileWithNoSpaces(File[] files) throws Exception
    {
        for(int i = 0; i < files.length; i++)
        {
            File f = new File(files[i].getParent() + File.separator + files[i].getName().replaceAll(" ","_"));
            files[i].renameTo(f);
            files[i] = f;
        }
    }
}
