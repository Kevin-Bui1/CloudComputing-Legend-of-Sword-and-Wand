package com.legends.pve.dto;

public class SaveCampaignRequest {
    private String partyName;
    private CampaignStateRequest state;

    public String getPartyName()                    { return partyName; }
    public void setPartyName(String partyName)      { this.partyName = partyName; }
    public CampaignStateRequest getState()          { return state; }
    public void setState(CampaignStateRequest s)    { this.state = s; }
}
