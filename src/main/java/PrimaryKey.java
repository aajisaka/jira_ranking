/**
 * Primary key for database.
 * The class is used to avoid duplicate primary key. 
 */
class PrimaryKey {
  private int issueNo;
  private String project;
  private String dateStr;

  PrimaryKey (int issueNo, String project, String dateStr) {
    this.issueNo = issueNo;
    this.project = project;
    this.dateStr = dateStr;
  }

  public int getIssueNo() {
    return issueNo;
  }

  public String getProject() {
    return project;
  }

  public String getDateStr() {
    return dateStr;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + issueNo;
    result = 31 * result + (project == null ? 0 : project.hashCode());
    result = 31 * result + (dateStr == null ? 0 : dateStr.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof PrimaryKey) {
      PrimaryKey key = (PrimaryKey) o;
      if (key.getIssueNo() == issueNo
          && key.getProject().equals(project)
          && key.getDateStr().equals(dateStr)) {
        return true;
      }
    }
    return false;
  }
}
