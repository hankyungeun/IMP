package com.bootest.dto.volume;

import lombok.Data;

@Data
public class DeleteVolumeDto {
    private String regionId;
    private String accountId;
    private String volumeId;
}
