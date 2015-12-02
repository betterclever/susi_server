/**
 *  MessageFactory
 *  Copyright 26.04.2015 by Michael Peter Christen, @0rb1t3r
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package org.loklak.data;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.loklak.tools.json.JSONObject;

public class MessageFactory extends AbstractIndexFactory<MessageEntry> implements IndexFactory<MessageEntry> {

    public MessageFactory(final Client elasticsearch_client, final String index_name, final int cacheSize) {
        super(elasticsearch_client, index_name, cacheSize);
    }

    @Override
    public XContentBuilder getMapping() {
        try {
            XContentBuilder mapping = XContentFactory.jsonBuilder()
              .startObject()
                .startObject("properties")
                  // fields which are common for all messages
                  .startObject("created_at").field("type","date").field("format","dateOptionalTime").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("screen_name").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject() // the screen_name is the nickname and unique in combination with 
                  .startObject("id_str").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("text").field("type","string").field("include_in_all","false").field("index","analyzed").endObject()

                  // these field are either scraped or can be reconstructucted from the context
                  .startObject("retweet_from").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject() // refers to a screen_name of the original author; indicates that this is a re-tweet of that user
                  .startObject("link").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject() // not to be indexed because it is not part of the content

                  // the next fields are used to distinguish source types and peer-to-peer steering
                  .startObject("source_type").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("provider_type").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("provider_hash").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  
                  // optional field for self-created content with copies to other social media platforms
                  .startObject("canonical_id").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject() // refers to a id_str if this message is a copy of the other message (the other message is an original twitter message and the refering message is generated by loklak)
                  
                  // fields which are only available from scrapes or API retrieval
                  .startObject("parent").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("place_name").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject() // contains either the place_name from twitter or the 'Title' from a GeoJson input
                  .startObject("place_id").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  
                  // the next set of fields can be scraped but could also be created automatically as an enrichment of given data
                  .startObject("on").field("type","date").field("format","dateOptionalTime").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("to").field("type","date").field("format","dateOptionalTime").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("retweet_count").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("favourites_count").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("links").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("links_count").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("images").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("images_count").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("audio").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("audio_count").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("videos").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("videos_count").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("campaign").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("campaign_count").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject()
                  
                  // *** everything below is created as enrichment ***
                  
                  // the following fields are either set as a common field or generated by extraction from field 'text' or from field 'place_name'
                  .startObject("place_country").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject() // the country code of the place, if known. Country codes must be 2-letter ISO-3166 in UPPERCASE
                  .startObject("place_context").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject() // the context describes if this is from ('from' context) or about ('about' context) a place
                  .startObject("location_point").field("type","geo_point").field("include_in_all","false").field("index","not_analyzed").endObject() // the exact location or the center of a location with a radius > 0, coordinate order is [longitude, latitude]
                  .startObject("location_radius").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject() // unit: meter. This is 0 (zero) if the location is a point and not a region
                  .startObject("location_mark").field("type","geo_point").field("include_in_all","false").field("index","not_analyzed").endObject() // if the location_point is not exact, this is a random location within the radius to paint a mark. If the location_point is exact, this is equal to location_point. coordinate order is [longitude, latitude]
                  .startObject("location_source").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject() // this string shows if the location was given by a user input("USER"), a technical input ("REPORT") or by a place-to-location translation ("ANNOTATION")
                  
                  // The following fields are extracted from field 'text' and shall not be in _all since 'text' is already in _all.
                  // TwitterRiver has a different structure here as well as the official twitter api, but that is a complex thing and not so good usable.
                  // We prefer a simple, flat structure for this metainfo.
                  // The Twitter API info about original and extracted links is also not usable here since we throw away the short links and replace them with extracted.
                  // Naming does not interfere with TwitterRiver, as far as visible.
                  .startObject("hosts").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("hosts_count").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("links").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("links_count").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("mentions").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("mentions_count").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("hashtags").field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("hashtags_count").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject()
                  
                  // classifier
                  .startObject("classifier_" + Classifier.Context.emotion).field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("classifier_" + Classifier.Context.emotion + "_probability").field("type","double").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("classifier_" + Classifier.Context.language).field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("classifier_" + Classifier.Context.language + "_probability").field("type","double").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("classifier_" + Classifier.Context.profanity).field("type","string").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("classifier_" + Classifier.Context.profanity + "_probability").field("type","double").field("include_in_all","false").field("index","not_analyzed").endObject()
                  
                  // experimental, for ranking
                  .startObject("without_l_len").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("without_lu_len").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject()
                  .startObject("without_luh_len").field("type","long").field("include_in_all","false").field("index","not_analyzed").endObject()
                .endObject()
              .endObject();
            return mapping;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public MessageEntry init(Map<String, Object> map) {
        return new MessageEntry(map);
    }

    //@Override
    public MessageEntry init(JSONObject json) {
        return new MessageEntry(json);
    }
    
}

