import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VIPContacts {
	int uid;
	final String pName;
	final String pTel;
	final int pAvatar;
	final String pSkype;
	
	public VIPContacts(int uid, final String pName, final String pTel, final int pAvatar,final String pSkype) {
		this.uid = uid;
		this.pName = pName;
		this.pTel = pTel;
		this.pAvatar = pAvatar;
		this.pSkype = pSkype;
	}
	List<Map> prepend() {
		String avatarAux="";
		if(pAvatar == 1)
			avatarAux = "http://"+Main.ip+":"+Main.port+"/avatars/parents/"+uid+".png";
		final List<Map>contacts = new ArrayList<Map>();
		Map<String, Object> parentMap = new HashMap<String, Object>(){{
			put("type", "parent");
			put("name",pName);
			put("tel",pTel);
			put("skype", pSkype);
		}};
		parentMap.put("avatar",avatarAux);
		contacts.add(parentMap);
		Map<String, Object> policeMap = new HashMap<String, Object>(){{
			put("type", "police");
			put("name","Polic&iacute;a");
			put("avatar","images/police.png");
		}};
		contacts.add(policeMap);
		Map<String, Object> fireMap = new HashMap<String, Object>(){{
			put("type", "fire");
			put("name","Bomberos");
			put("avatar","images/fire.png");
		}};
		contacts.add(fireMap);
		
		Map<String, Object> medicMap = new HashMap<String, Object>(){{
			put("type", "medic");
			put("name","M&eacute;dico");
			put("avatar","images/medic.png");
		}};
		contacts.add(medicMap);
		
		return contacts;
	}
}
