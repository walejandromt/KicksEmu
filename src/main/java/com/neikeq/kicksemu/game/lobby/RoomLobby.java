package com.neikeq.kicksemu.game.lobby;

import java.util.ArrayList;
import java.util.List;

public class RoomLobby implements Lobby {

    private final List<Integer> players = new ArrayList<>();
    private final Object locker = new Object();

    private boolean teamChatEnabled = true;

    public List<Integer> getPlayers() {
        synchronized (locker) {
            return players;
        }
    }

    @Override
    public void addPlayer(int playerId) {
        synchronized (locker) {
            if (!players.contains(playerId)) {
                players.add(playerId);
            }
        }
    }

    @Override
    public void removePlayer(int playerId) {
        synchronized (locker) {
            int index = players.indexOf(playerId);

            if (index >= 0) {
                players.remove(index);
            }
        }
    }

    public boolean isTeamChatEnabled() {
        return teamChatEnabled;
    }

    public void setTeamChatEnabled(boolean teamChatEnabled) {
        this.teamChatEnabled = teamChatEnabled;
    }
}
