package com.cheng.game.network.handler;

import com.cheng.game.network.codec.GamePacket;
import com.cheng.game.network.session.PlayerDisconnectedEvent;
import com.cheng.game.network.session.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@ChannelHandler.Sharable
@Component
public class GameServerHandler extends SimpleChannelInboundHandler<GamePacket> {

    private static final Logger log = LoggerFactory.getLogger(GameServerHandler.class);

    private final MessageDispatcher dispatcher;
    private final SessionManager sessionManager;
    private final ApplicationEventPublisher eventPublisher;

    public GameServerHandler(MessageDispatcher dispatcher,
                             SessionManager sessionManager,
                             ApplicationEventPublisher eventPublisher) {
        this.dispatcher = dispatcher;
        this.sessionManager = sessionManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("Channel active: {}", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GamePacket msg) {
        dispatcher.dispatch(ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Long playerId = ctx.channel().attr(SessionManager.PLAYER_ID).get();
        sessionManager.unbind(ctx.channel());
        if (playerId != null) {
            eventPublisher.publishEvent(new PlayerDisconnectedEvent(playerId));
        }
        log.info("Channel inactive: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event && event.state() == IdleState.READER_IDLE) {
            log.info("Reader idle, closing channel {}", ctx.channel().remoteAddress());
            ctx.close();
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("Channel exception {}: {}", ctx.channel().remoteAddress(), cause.toString());
        ctx.close();
    }
}
