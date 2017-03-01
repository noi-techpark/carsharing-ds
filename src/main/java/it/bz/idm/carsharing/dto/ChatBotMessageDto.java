package it.bz.idm.carsharing.dto;

import java.util.ArrayList;
import java.util.List;

public class ChatBotMessageDto {
	List<ChatBotTextDto> messages;
	
	public ChatBotMessageDto(){
		
	}

	public List<ChatBotTextDto> getMessages() {
		if(messages == null)
			messages = new ArrayList<>();
		return messages;
	}

	public void setMessages(List<ChatBotTextDto> messages) {
		this.messages = messages;
	}
}
