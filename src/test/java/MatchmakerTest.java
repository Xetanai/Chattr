import moe.xetanai.chattr.entities.Search;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

public class MatchmakerTest {
	private static Logger LOG = LoggerFactory.getLogger("Matchmaker Test");

	@Test
	public void testCompatibility() {
		Search a = new Search(null, "a","b","c","d","e","f","g","h");
		Search b = new Search(null, "A","B","C","D","E","F","G","H");
		Search c = new Search(null, "i","j","k","l","m","n","o","p");
		Search d = new Search(null, "e","f","g","h","i","j","k","l");

		LOG.info("Case sensitivity test.");
		Assert.assertEquals(1,a.getCompatibility(b),0);

		LOG.info("No compatibility test.");
		Assert.assertEquals(0,a.getCompatibility(c),0);

		LOG.info("Partial match test 1.");
		Assert.assertEquals(0.33,a.getCompatibility(d),0.01);

		LOG.info("Partial match test 2.");
		Assert.assertEquals(0.33, c.getCompatibility(d), 0.01);
	}
}
