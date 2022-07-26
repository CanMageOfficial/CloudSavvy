package com.cloudSavvy.aws.common;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import static org.junit.jupiter.api.Assertions.*;

public class EntityLinksTest {
    @Test
    public void test_entityLinkTemplateCounts() {
        for (EntityType entityType : EntityType.values()) {
            assertTrue(EntityLinks.ENTITY_LINK_TEMPLATES.containsKey(entityType), entityType.toString());
            assertTrue(EntityLinks.SERVICE_LINK_TEMPLATES.containsKey(entityType), entityType.toString());
        }
    }

    @Test
    public void test_getEntityLink() {
        for (EntityType entityType : EntityType.values()) {
            String entityLink;
            if (entityType.isContainer()) {
                entityLink = EntityLinks.getEntityLink(Region.US_EAST_1, entityType, "container", "test");
            } else {
                entityLink = EntityLinks.getEntityLink(Region.US_EAST_1, entityType, "test");
            }
            assertNotNull(entityLink, entityType.toString());
        }
    }

    @Test
    public void test_getServiceLink() {
        for (EntityType entityType : EntityType.values()) {
            String serviceLink = EntityLinks.getServiceLink(Region.US_EAST_1, entityType);
            assertNotNull(serviceLink, entityType.toString());
        }
    }

    @Test
    public void test_getLink() {
        String link = EntityLinks.getEntityLink(Region.US_EAST_1, EntityType.DynamoDB_TABLE, "test");
        assertEquals("https://us-east-1.console.aws.amazon.com/dynamodbv2/home?region=us-east-1#table?name=test", link);
    }

    @Test
    public void test_getS3ObjectPrefixLink() {
        String objectLink = EntityLinks.getS3ObjectPrefixLink("cloudsavvy-771793231253-us-east-2",
                "us-east-2", "2022-07-25_10-26-37/Error_Data.html");
        assertEquals("https://s3.console.aws.amazon.com/s3/object/cloudsavvy-771793231253-us-east-2?region=us-east-2&prefix=2022-07-25_10-26-37/Error_Data.html", objectLink);
    }
}
