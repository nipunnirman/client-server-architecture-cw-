package com.smartcampus.api.resource;

import com.smartcampus.api.exception.RoomNotEmptyException;
import com.smartcampus.api.model.ApiError;
import com.smartcampus.api.model.Room;
import com.smartcampus.api.store.InMemoryStore;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final Map<String, Room> rooms = InMemoryStore.rooms();

    @GET
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    @POST
    public Response createRoom(Room room, @jakarta.ws.rs.core.Context UriInfo uriInfo) {
        if (room == null || isBlank(room.getId()) || isBlank(room.getName())) {
            ApiError error = new ApiError("INVALID_ROOM", "id and name are required");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        if (rooms.containsKey(room.getId())) {
            ApiError error = new ApiError("ROOM_EXISTS", "Room with this id already exists");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        rooms.put(room.getId(), room);
        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(location).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Room getRoomById(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found: " + roomId);
        }
        return room;
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found: " + roomId);
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room cannot be deleted while sensors are assigned");
        }

        rooms.remove(roomId);
        return Response.noContent().build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
