package com.cloudSavvy.common;

import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@AllArgsConstructor
@ToString
@Getter
@NoArgsConstructor
public class IssueData {
    private EntityType entityType;
    private String containerName;
    private String entityName;
    private IssueCode issueCode;

    public IssueData(EntityType entityType, String entityName, IssueCode issueCode) {
        this.entityType = entityType;
        this.entityName = entityName;
        this.issueCode = issueCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        IssueData issueData = (IssueData) obj;
        return entityType == issueData.getEntityType() &&
                Objects.equals(containerName, issueData.getContainerName()) &&
                Objects.equals(entityName, issueData.getEntityName()) &&
                issueCode == issueData.getIssueCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, containerName, entityName, issueCode);
    }
}
