/**
 * 
 */
package helper;

/**
 * @author aquast
 *
 */
public class W3CDummyLabelResolver implements LabelResolver {

    public static final String DOMAIN = "www.w3.org";

    @Override
    public String lookup(String urlString, String Language) {
        // TODO Auto-generated method stub
        play.Logger.debug("This is the DummyLabelResolver: Just doing nothing");
        return urlString;
    }

}
