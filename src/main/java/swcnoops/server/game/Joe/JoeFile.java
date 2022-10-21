package swcnoops.server.game.Joe;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.Id;
import swcnoops.server.game.CampaignData;
import swcnoops.server.game.CampaignMissionData;
import swcnoops.server.game.TournamentData;
import swcnoops.server.game.TroopData;

import java.util.Date;
import java.util.List;

public class JoeFile {
    @Id
    private String fileName;
    private boolean fileGenerated;
    private Date generatedDate;

    private Content content = new Content();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(Date generatedDate) {
        this.generatedDate = generatedDate;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public boolean isFileGenerated() {
        return fileGenerated;
    }

    public void setFileGenerated(boolean fileGenerated) {
        this.fileGenerated = fileGenerated;
    }

    static public class Content {
        private JoeObjects objects = new JoeObjects();

        public JoeObjects getObjects() {
            return objects;
        }

        public void setObjects(JoeObjects objects) {
            this.objects = objects;
        }
    }

    static public class JoeObjects {
        @JsonProperty("CampaignData")
        private List<CampaignData> campaignData;
        @JsonProperty("CampaignMissionData")
        private List<CampaignMissionData> campaignMissionData;
        @JsonProperty("TournamentData")
        private List<TournamentData> tournamentData;

        public List<CampaignData> getCampaignData() {
            return campaignData;
        }

        public void setCampaignData(List<CampaignData> campaignData) {
            this.campaignData = campaignData;
        }

        public List<CampaignMissionData> getCampaignMissionData() {
            return campaignMissionData;
        }

        public void setCampaignMissionData(List<CampaignMissionData> campaignMissionData) {
            this.campaignMissionData = campaignMissionData;
        }

        public List<TournamentData> getTournamentData() {
            return tournamentData;
        }

        public void setTournamentData(List<TournamentData> tournamentData) {
            this.tournamentData = tournamentData;
        }
    }
}
