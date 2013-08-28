package org.foxteam.noisyfox.patronsline;

import java.util.List;

public class InformationSession {

	String uid;
	String session;

	InformationUser user;

	List<InformationShop> ownedShop;// 所管理的店铺，如果是商家则需要该参数

	// 收藏的菜肴，如果是食客则需要这个参数
	List<InformationBookmarkShop> bookmarkShop;
	List<InformationBookmarkFood> bookmarkFood;

}
