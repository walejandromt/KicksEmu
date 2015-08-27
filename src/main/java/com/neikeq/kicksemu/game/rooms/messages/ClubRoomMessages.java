package com.neikeq.kicksemu.game.rooms.messages;

import com.neikeq.kicksemu.game.characters.PlayerInfo;
import com.neikeq.kicksemu.game.clubs.MemberInfo;
import com.neikeq.kicksemu.game.lobby.LobbyManager;
import com.neikeq.kicksemu.game.rooms.ClubRoom;
import com.neikeq.kicksemu.game.rooms.Room;
import com.neikeq.kicksemu.game.rooms.RoomManager;
import com.neikeq.kicksemu.game.rooms.enums.RoomBall;
import com.neikeq.kicksemu.game.rooms.enums.RoomMap;
import com.neikeq.kicksemu.game.rooms.enums.RoomMode;
import com.neikeq.kicksemu.game.rooms.enums.RoomSize;
import com.neikeq.kicksemu.game.rooms.enums.RoomAccessType;
import com.neikeq.kicksemu.game.servers.ServerType;
import com.neikeq.kicksemu.game.sessions.Session;
import com.neikeq.kicksemu.game.users.UserInfo;
import com.neikeq.kicksemu.network.packets.in.ClientMessage;
import com.neikeq.kicksemu.network.packets.out.MessageBuilder;
import com.neikeq.kicksemu.network.packets.out.ServerMessage;
import com.neikeq.kicksemu.network.server.ServerManager;

public class ClubRoomMessages extends RoomMessages {

    private static final int MAX_ROOM_NAME_LENGTH = 14;
    static final byte MIN_ROOM_LEVEL = 3;

    public static void createRoom(Session session, ClientMessage msg) {
        RoomAccessType type = RoomAccessType.fromShort(msg.readShort());
        String name = msg.readString(15);
        String password = msg.readString(5);

        int playerId = session.getPlayerId();
        int clubId = MemberInfo.getClubId(playerId);

        RoomMode roomMode = RoomMode.fromInt(msg.readByte());

        // Check that everything is correct
        byte result = 0;

        ServerType serverType = ServerManager.getServerType();

        if (type == null || roomMode == null || roomMode.notValidForServer(serverType)) {
            result = (byte) -1; // System problem
        } else if (PlayerInfo.getLevel(playerId) < MIN_ROOM_LEVEL) {
            result = (byte) -3; // Does not meet the level requirements
        } else if (clubId <= 0) {
            result = (byte) -4; // Not a club member
        } else if (RoomManager.getRoomById(clubId) != null) {
            result = (byte) -5; // The club already has a team
        }

        // TODO Check result -2: Too many players in the opponent team. What does that even mean?

        // Send the result to the client
        session.send(MessageBuilder.clubCreateRoom((short) 0, result));

        // If everything is correct, create the room
        if (result == 0) {
            ClubRoom room = new ClubRoom();

            // Limit the length of the name and the password
            if (name.length() > MAX_ROOM_NAME_LENGTH) {
                name = name.substring(0, MAX_ROOM_NAME_LENGTH);
            }

            if (password.length() > MAX_ROOM_PASSWORD_LENGTH) {
                password = password.substring(0, MAX_ROOM_PASSWORD_LENGTH);
            }

            // If password is blank, disable password usage
            if (type == RoomAccessType.PASSWORD && password.isEmpty()) {
                type = RoomAccessType.FREE;
            }

            // Set room information from received data
            room.setName(name);
            room.setPassword(password);
            room.setAccessType(type);
            room.setRoomMode(roomMode);
            room.setMinLevel(MIN_ROOM_LEVEL);
            room.setMaxLevel(MAX_ROOM_LEVEL);
            room.setMap(RoomMap.RESERVOIR);
            room.setBall(RoomBall.TEAM_ARENA);
            room.setMaxSize(RoomSize.SIZE_4V4);

            synchronized (RoomManager.ROOMS_LOCKER) {
                // Get the room id
                room.setId(clubId);
                // Add it to the rooms list
                RoomManager.addRoom(room);
                // Add the player to the room
                room.addPlayer(session);
            }
        }
    }

    public static void joinRoom(Session session, ClientMessage msg) {
        if (session.getRoomId() > 0) return;

        int roomId = msg.readShort();
        String password = msg.readString(4);

        Room room = RoomManager.getRoomById(roomId);

        // Try to join the room.
        if (room != null) {
            room.tryJoinRoom(session, password);
        } else {
            // Result -3 means that the room does not exists.
            session.send(MessageBuilder.clubJoinRoom(null, (byte) -3));
        }
    }

    public static void roomList(Session session, ClientMessage msg) {
        short page = msg.readShort();
        session.send(MessageBuilder.clubRoomList(RoomManager.getRoomsFromPage(page),
                page, (byte) 0));
    }

    public static void roomSettings(Session session, ClientMessage msg) {
        int roomId = msg.readShort();
        RoomAccessType type = RoomAccessType.fromShort(msg.readShort());
        String name = msg.readString(15);
        String password = msg.readString(5);

        // Check that settings are valid

        byte result = 0;

        Room room = RoomManager.getRoomById(roomId);

        if (type == null) {
            result = (byte) -1; // System problem
        } else if (room == null) {
            result = (byte) -2; // Room does not exist
        } else if (room.getMaster() != session.getPlayerId()) {
            // Player is not room's master
            // Actually, it will display the same as -2...
            result = (byte) -3;
        } else {
            // Limit the length of the name and the password
            if (name.length() > MAX_ROOM_NAME_LENGTH) {
                name = name.substring(0, MAX_ROOM_NAME_LENGTH);
            }

            if (password.length() > MAX_ROOM_PASSWORD_LENGTH) {
                password = password.substring(0, MAX_ROOM_PASSWORD_LENGTH);
            }

            // Update room settings
            room.setAccessType(type);
            room.setName(name);
            room.setPassword(password);

            room.sendBroadcast(MessageBuilder.clubRoomSettings(room, result));
        }

        if (result != 0) {
            session.send(MessageBuilder.clubRoomSettings(room, result));
        }
    }

    public static void invitePlayer(Session session, ClientMessage msg) {
        Room room = RoomManager.getRoomById(session.getRoomId());

        // If the player is in a room
        if (room != null) {
            int playerToInvite = msg.readInt();

            byte result = 0;

            if (room.isNotFull()) {
                // If the player to invite is in the main lobby
                if (LobbyManager.getMainLobby().getPlayers().contains(playerToInvite)) {
                    Session sessionToInvite = ServerManager.getPlayers().get(playerToInvite);

                    if (UserInfo.getSettings(sessionToInvite.getUserId()).getInvites()) {
                        int targetClubId = MemberInfo.getClubId(playerToInvite);

                        // If the target player is a member of the club
                        if (targetClubId == room.getId()) {
                            ServerMessage invitation = MessageBuilder.clubInvitePlayer(result,
                                    room, PlayerInfo.getName(session.getPlayerId()));
                            sessionToInvite.sendAndFlush(invitation);
                        } else {
                            result = (byte) -6; // Target player is not a member of the club
                        }
                    } else {
                        result = (byte) -3; // Target player does not accept invitations
                    }
                } else {
                    result = (byte) -2; // Target player not found
                }
            }

            // If there is something wrong, notify the client
            if (result != 0) {
                session.send(MessageBuilder.clubInvitePlayer(result, null, ""));
            }
        }
    }
}
