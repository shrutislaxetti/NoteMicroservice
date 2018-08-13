package com.bridgelabz.notemicroservice.util;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import com.bridgelabz.notemicroservice.exceptions.GetLinkInfoException;
import com.bridgelabz.notemicroservice.model.URLMetaData;

@Component
public class LinkInfoProvider {

	public URLMetaData getLinkInformation(String link) throws GetLinkInfoException {
		Document doc = null;
		String description = null;
		String imageUrl = null;
		try {
			doc = Jsoup.connect(link).get();
			description = doc.select("meta[name=description]").first().attr("content");
			imageUrl = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]").attr("src");
		} catch (IOException exception) {
			throw new GetLinkInfoException("unable to fetch link information");
		}

		URLMetaData urlInfo = new URLMetaData();
		urlInfo.setLink(link);
		urlInfo.setImageURL(imageUrl);
		urlInfo.setDescription(description);

		return urlInfo;
	}
}
