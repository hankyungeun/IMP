package com.bootest.util;

import java.util.ArrayList;
import java.util.List;

import com.bootest.dto.TagDto;

import software.amazon.awssdk.services.ec2.model.Tag;

public class VrmTags {
    public static List<TagDto> getEc2Tags(List<Tag> tags) {

        List<TagDto> results = new ArrayList<>();

        for (Tag tag : tags) {

            TagDto vrmTag = new TagDto();
            vrmTag.setKey(tag.key());
            vrmTag.setValue(tag.value());

            results.add(vrmTag);
        }
        return results;
    }

}
