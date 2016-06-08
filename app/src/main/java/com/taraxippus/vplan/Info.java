package com.taraxippus.vplan;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import android.text.format.DateFormat;

public abstract class Info
{
	public static final String URL_TODAY = "http://306.joomla.schule.bremen.de/ServerSync/V-Plan-heute.htm";
	public static final String URL_TOMORROW = "http://306.joomla.schule.bremen.de/ServerSync/V-Plan-morgen.htm";
	public static final String URL_MENU = "https://docs.google.com/document/d/1oC8Qe8_MFu8HP412nfiMuC_53F2rTXn2TJRsMpZg0F4/edit?usp=sharing";
	
	public static final java.text.DateFormat htmlDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
	
	public static final int[] TABS = new int[] {R.string.today, R.string.tomorrow};
	
	public static final HashMap<String, String> TEACHERS = new HashMap<>();

	static
	{
		TEACHERS.put("Asl", "Aslan");
		TEACHERS.put("Bac", "Bachmann");
		TEACHERS.put("Bal", "Balster");
		TEACHERS.put("Bas", "Basedow");
		TEACHERS.put("Bcm", "Beckmann");
		TEACHERS.put("Bes", "von Bestenbostel");
		TEACHERS.put("Brt", "Birth");
		TEACHERS.put("Böh", "Böhm");
		TEACHERS.put("Bnt", "Brannath");
		TEACHERS.put("Gom", "Cano Gómez");
		TEACHERS.put("DA", "Dall Asta");
		TEACHERS.put("Dit", "Dittrich");
		TEACHERS.put("Dry", "Dreyer");
		TEACHERS.put("Ehl", "Ehlers");
		TEACHERS.put("Fng", "Fangmann");
		TEACHERS.put("Far", "Farke");
		TEACHERS.put("Gev", "Gevers");
		TEACHERS.put("Gro", "Groothius");
		TEACHERS.put("Grü", "Grüschow");
		TEACHERS.put("Hah", "Hahn");
		TEACHERS.put("Hai", "Haiduck");
		TEACHERS.put("Han", "Hain");
		TEACHERS.put("Ham", "Hamann");
		TEACHERS.put("Haf", "Harf");
		TEACHERS.put("Hsh", "Heilshorn");
		TEACHERS.put("Hsm", "Hesemann");
		TEACHERS.put("Hle", "Hille");
		TEACHERS.put("Hub", "Hubig");
		TEACHERS.put("Ktv", "Kalitovics");
		TEACHERS.put("Kau", "Kaufhold");
		TEACHERS.put("Kli", "Klingbeil");
		TEACHERS.put("Kön", "König");
		TEACHERS.put("Küt", "Küthmann");
		TEACHERS.put("Lan", "Lange");
		TEACHERS.put("Leh", "Lehmann");
		TEACHERS.put("Leo", "Leo");
		TEACHERS.put("Mmr", "Marxmeier");
		TEACHERS.put("Mey", "Meyer");
		TEACHERS.put("Mlr", "Marielle Müller");
		TEACHERS.put("Mür", "Verena Müller");
		TEACHERS.put("Orl", "Orlik");
		TEACHERS.put("Qui", "Quiring");
		TEACHERS.put("Red", "Rediske");
		TEACHERS.put("Rem", "Reimer");
		TEACHERS.put("RK", "Riemann-Kurtz");
		TEACHERS.put("San", "Sander");
		TEACHERS.put("Shb", "Scheibe");
		TEACHERS.put("Sla", "Schlachter");
		TEACHERS.put("Sct", "Schulte");
		TEACHERS.put("Shw", "Schwarze");
		TEACHERS.put("Spi", "Spiecker");
		TEACHERS.put("Ste", "Steinberg");
		TEACHERS.put("Tlb", "Thielebein");
		TEACHERS.put("Tre", "Treichel");
		TEACHERS.put("Les", "van Lessen");
		TEACHERS.put("Vig", "Vignais");
		TEACHERS.put("Wlb", "Wallbach");
		TEACHERS.put("Wes", "Wessels");
		TEACHERS.put("Wnh", "Windhorst");
		TEACHERS.put("Wit", "Witmeier");
		TEACHERS.put("Xan", "Xanthopoulos");
		TEACHERS.put("Zha", "Zhang");
	}
	
	public static String formatDate(Context context, Date date)
	{
		if (date.getDay() != new Date().getDay())
			return DateFormat.getDateFormat(context).format(date);
			
		return DateFormat.getTimeFormat(context).format(date);
	}
}
