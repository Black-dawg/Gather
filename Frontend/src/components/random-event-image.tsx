import { useEffect, useState } from "react";

interface RandomEventImageProps {
  eventName?: string;
}

const RandomEventImage: React.FC<RandomEventImageProps> = ({ eventName }) => {
  const [imageSrc, setImageSrc] = useState("");

  useEffect(() => {
    if (eventName) {
      const lower = eventName.toLowerCase();
      if (
        lower.includes("tech") ||
        lower.includes("innovators") ||
        lower.includes("summit") ||
        lower.includes("conference") ||
        lower.includes("hackathon")
      ) {
        // Tech presentation / conference image
        setImageSrc("/event-image-1.webp");
      } else if (
        lower.includes("music") ||
        lower.includes("festival") ||
        lower.includes("beats") ||
        lower.includes("concert") ||
        lower.includes("party")
      ) {
        // Music concert / crowd image
        setImageSrc("/event-image-3.webp");
      } else {
        // Deterministic fallback based on name length
        const index = (eventName.length % 4) + 1;
        setImageSrc(`/event-image-${index}.webp`);
      }
    } else {
      // Fallback if no eventName is provided
      const randomIndex = Math.floor(Math.random() * 4) + 1;
      setImageSrc(`/event-image-${randomIndex}.webp`);
    }
  }, [eventName]);

  return <img src={imageSrc} alt="Event" className="object-cover w-full h-full" />;
};

export default RandomEventImage;
