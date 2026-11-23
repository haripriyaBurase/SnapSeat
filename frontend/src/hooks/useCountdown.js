
import { useEffect, useState } from "react";

function useCountdown(lockedUntil) {

    const calculateTimeLeft = () => {
        if (!lockedUntil) return 0;

        const difference =
            new Date(lockedUntil).getTime() - Date.now();

        return Math.max(0, Math.floor(difference / 1000));
    };

    const [timeLeft, setTimeLeft] = useState(calculateTimeLeft);

    useEffect(() => {

        if (!lockedUntil) {
            return;
        }

        const interval = setInterval(() => {

            const remaining =
                Math.max(
                    0,
                    Math.floor(
                        (new Date(lockedUntil).getTime() - Date.now()) / 1000
                    )
                );

            setTimeLeft(remaining);

            if (remaining <= 0) {
                clearInterval(interval);
            }

        }, 1000);

        return () => clearInterval(interval);

    }, [lockedUntil]);

    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;

    const formattedTime =
        `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;

    return {
        timeLeft,
        formattedTime,
        expired: timeLeft <= 0
    };
}

export default useCountdown;
