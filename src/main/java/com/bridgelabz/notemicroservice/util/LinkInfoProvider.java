package com.bridgelabz.notemicroservice.util;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import com.bridgelabz.notemicroservice.exceptions.LinkInformationException;
import com.bridgelabz.notemicroservice.model.URLMetaData;

@Component
public class LinkInfoProvider {

	public URLMetaData getLinkInformation(String link) throws LinkInformationException {
		 
		String description = null;
		String imageUrl = null;
		try {
			Document document = Jsoup.connect(link).get();
			description = document.select("meta[name=description]").first().attr("content");
			imageUrl = document.select("img[src~=(?i)\\.(png|jpe?g|gif)]").attr("src");
		} catch (IOException exception) {
			throw new LinkInformationException("Failed to fetch information");
		}

		URLMetaData info = new URLMetaData();
		info.setLink(link);
		info.setImageURL(imageUrl);
		info.setDescription(description);

		return info;
	}
}
