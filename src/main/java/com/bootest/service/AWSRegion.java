package com.bootest.service;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.regions.Region;

@Service
public class AWSRegion {
    public Region getRegion(String regionCode) {
        switch (regionCode) {
            case "us-east-1":
                return Region.US_EAST_1;
            case "us-east-2":
                return Region.US_EAST_2;
            case "us-west-1":
                return Region.US_WEST_1;
            case "us-west-2":
                return Region.US_WEST_2;
            case "af-south-1":
                return Region.AF_SOUTH_1;
            case "ap-east-1":
                return Region.AP_EAST_1;
            case "ap-southeast-1":
                return Region.AP_SOUTHEAST_1;
            case "ap-southeast-2":
                return Region.AP_SOUTHEAST_2;
            case "ap-northeast-1":
                return Region.AP_NORTHEAST_1;
            case "ap-northeast-2":
                return Region.AP_NORTHEAST_2;
            case "ap-northeast-3":
                return Region.AP_NORTHEAST_3;
            case "ca-central-1":
                return Region.CA_CENTRAL_1;
            case "eu-central-1":
                return Region.EU_CENTRAL_1;
            case "eu-north-1":
                return Region.EU_NORTH_1;
            case "eu-south-1":
                return Region.EU_SOUTH_1;
            case "eu-west-1":
                return Region.EU_WEST_1;
            case "eu-west-2":
                return Region.EU_WEST_2;
            case "eu-west-3":
                return Region.EU_WEST_3;
            case "me-south-1":
                return Region.ME_SOUTH_1;
            case "sa-east-1":
                return Region.SA_EAST_1;
            default:
                return Region.AWS_GLOBAL;
        }
    }

    public String getLoactions(String regionCode) {
        switch (regionCode) {
            case "us-east-1":
                return "US East (N. Virginia)";
            case "us-east-2":
                return "US East (Ohio)";
            case "us-west-1":
                return "US West (N. California)";
            case "us-west-2":
                return "US West (Oregon)";
            case "af-south-1":
                return "Africa (Cape Town)";
            case "ap-east-1":
                return "Asia Pacific (Hong Kong)";
            case "ap-south-1":
                return "Asia Pacific (Mumbai)";
            case "ap-southeast-1":
                return "Asia Pacific (Singapore)";
            case "ap-southeast-2":
                return "Asia Pacific (Sydney)";
            case "ap-southeast-3":
                return "Asia Pacific (Jakarta)";
            case "ap-northeast-1":
                return "Asia Pacific (Tokyo)";
            case "ap-northeast-2":
                return "Asia Pacific (Seoul)";
            case "ap-northeast-3":
                return "Asia Pacific (Osaka)";
            case "ca-central-1":
                return "Canada (Central)";
            case "eu-central-1":
                return "Europe (Frankfurt)";
            case "eu-north-1":
                return "Europe (Stockholm)";
            case "eu-south-1":
                return "Europe (Milan)";
            case "eu-west-1":
                return "Europe (Ireland)";
            case "eu-west-2":
                return "Europe (London)";
            case "eu-west-3":
                return "Europe (Paris)";
            case "me-south-1":
                return "Middle East (Bahrain)";
            case "sa-east-1":
                return "South America (São Paulo)";
            default:
                return "Global";
        }
    }

    public String getRegionCode(String regionCode) {
        switch (regionCode) {
            case "us-east-1":
                return "USE1";
            case "us-east-2":
                return "USE2";
            case "us-west-1":
                return "USW1";
            case "us-west-2":
                return "USW2";
            case "af-south-1":
                return "AFS1";
            case "ap-east-1":
                return "APE1";
            case "ap-southeast-1":
                return "APS1";
            case "ap-southeast-2":
                return "APS2";
            case "ap-southeast-3":
                return "APS3";
            case "ap-northeast-1":
                return "APN1";
            case "ap-northeast-2":
                return "APN2";
            case "ap-northeast-3":
                return "APN3";
            case "ca-central-1":
                return "CAC1";
            case "eu-central-1":
                return "EUC1";
            case "eu-west-1":
                return "EUW1";
            case "eu-west-2":
                return "EUW2";
            case "eu-west-3":
                return "EUW3";
            case "eu-south-1":
                return "EUS1";
            case "eu-north-1":
                return "EUN1";
            case "me-south-1":
                return "MES1";
            case "sa-east-1":
                return "SAE1";
            default:
                return null;
        }
    }
}
