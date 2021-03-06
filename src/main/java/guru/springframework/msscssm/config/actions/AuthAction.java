package guru.springframework.msscssm.config.actions;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Random;

import static guru.springframework.msscssm.domain.PaymentEvent.*;
import static guru.springframework.msscssm.service.PaymentServiceImpl.PAYMENT_ID_HEADER;

@Component
public class AuthAction implements Action<PaymentState, PaymentEvent> {
    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> stateContext) {

        if (new Random().nextInt(10) < 3) {
            stateContext.getStateMachine()
                    .sendEvent(MessageBuilder.withPayload(AUTH_DECLINED)
                            .setHeader(PAYMENT_ID_HEADER, stateContext.getMessageHeader(PAYMENT_ID_HEADER))
                            .build());
        } else {
            stateContext.getStateMachine()
                    .sendEvent(MessageBuilder.withPayload(AUTH_APPROVED)
                            .setHeader(PAYMENT_ID_HEADER, stateContext.getMessageHeader(PAYMENT_ID_HEADER))
                            .build());
        }
    }
}
